package dev.softwarefactory.chatter.state

import dev.softwarefactory.chatter.core.Call
import dev.softwarefactory.chatter.core.Chat
import dev.softwarefactory.chatter.core.ChatKind
import dev.softwarefactory.chatter.core.Contact
import dev.softwarefactory.chatter.core.Message
import dev.softwarefactory.chatter.core.MessageStatus
import dev.softwarefactory.chatter.core.advanceStatus
import dev.softwarefactory.chatter.core.buildSeed
import dev.softwarefactory.chatter.core.draftMessage
import dev.softwarefactory.chatter.core.toggleReaction
import dev.softwarefactory.chatter.core.withMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicInteger

/** Port of src/state/store.ts. Storage key and JSON shape are the same as the React Native app's. */
const val STORAGE_KEY = "chatter:v1"

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Profile(@EncodeDefault val name: String = "You", @EncodeDefault val about: String = "Available")

data class AppState(
    val hydrated: Boolean = false,
    val profile: Profile = Profile(),
    val contacts: Map<String, Contact> = emptyMap(),
    val chats: List<Chat> = emptyList(),
    val calls: List<Call> = emptyList(),
)

/** What is stored: {profile, contacts, chats, calls}. Every field tolerates being absent. */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class StoredState(
    @EncodeDefault val profile: Profile = Profile(),
    @EncodeDefault val contacts: Map<String, Contact> = emptyMap(),
    @EncodeDefault val chats: List<Chat> = emptyList(),
    @EncodeDefault val calls: List<Call> = emptyList(),
)

/** Async string storage (AsyncStorage's shape). Android implementation: SharedPreferences. */
interface KeyValueStorage {
    suspend fun getItem(key: String): String?
    suspend fun setItem(key: String, value: String)
    suspend fun removeItem(key: String)
}

class InMemoryStorage(val items: MutableMap<String, String> = mutableMapOf()) : KeyValueStorage {
    override suspend fun getItem(key: String) = items[key]
    override suspend fun setItem(key: String, value: String) { items[key] = value }
    override suspend fun removeItem(key: String) { items.remove(key) }
}

val storeJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = true
}

val REPLIES = listOf("👍", "Sounds good!", "Haha 😄", "On it", "Talk later?", "Nice!", "Sure thing")

/**
 * @param scope runs the status timers, replies and saves; tests pass a scope on a virtual-time dispatcher.
 * @param clock current time in epoch milliseconds.
 */
class AppStore(
    private val storage: KeyValueStorage,
    private val scope: CoroutineScope,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val saveLock = Mutex()
    private val counter = AtomicInteger(0)

    private fun set(persist: Boolean = true, change: (AppState) -> AppState) {
        val next = run { _state.update(change); _state.value }
        if (persist && next.hydrated) save()
    }

    // Always writes the newest state at the time the write runs, so reordered saves can't leave stale data.
    private fun save() {
        scope.launch {
            saveLock.withLock {
                val s = _state.value
                storage.setItem(STORAGE_KEY, storeJson.encodeToString(StoredState.serializer(), StoredState(s.profile, s.contacts, s.chats, s.calls)))
            }
        }
    }

    /** Loads what was saved, or the sample conversations on a first launch. */
    suspend fun hydrate(now: Long = clock()) {
        val saved = storage.getItem(STORAGE_KEY)?.let { raw ->
            runCatching { storeJson.decodeFromString(StoredState.serializer(), raw) }.getOrNull()
        }
        if (saved != null) {
            set(persist = false) { AppState(true, saved.profile, saved.contacts, saved.chats, saved.calls) }
        } else {
            val seed = buildSeed(now)
            set { AppState(true, it.profile, seed.contacts, seed.chats, seed.calls) }
        }
    }

    fun chatById(id: String): Chat? = _state.value.chats.firstOrNull { it.id == id }

    private fun replaceChat(chat: Chat) = set { s -> s.copy(chats = s.chats.map { if (it.id == chat.id) chat else it }) }

    fun markRead(chatId: String) {
        val chat = chatById(chatId)
        if (chat != null && chat.unread > 0) replaceChat(chat.copy(unread = 0))
    }

    private fun newId(prefix: String) = "$prefix-${clock()}-${counter.incrementAndGet()}"

    /** Sends my message; its ticks move on, and in a direct chat the other person may answer. */
    fun sendMessage(chatId: String, text: String, now: Long = clock()) {
        val chat = chatById(chatId) ?: return
        val message = draftMessage(text, now, newId("m"))
        replaceChat(withMessage(chat, message))
        val step = {
            chatById(chatId)?.let { current ->
                replaceChat(current.copy(messages = current.messages.map { if (it.id == message.id) it.copy(status = advanceStatus(it.status)) else it }))
            }
        }
        for (after in longArrayOf(400, 1200, 2600)) scope.launch { delay(after); step() }
        if (chat.kind == ChatKind.Direct) {
            scope.launch {
                delay(4200)
                val current = chatById(chatId) ?: return@launch
                val reply = Message(newId("r"), chat.members[0], REPLIES[current.messages.size % REPLIES.size], clock(), MessageStatus.Read)
                replaceChat(current.copy(messages = current.messages + reply))
            }
        }
    }

    /** Opens the direct chat with a contact, starting one if there is none yet. Returns its id. */
    fun startChat(contactId: String, now: Long = clock()): String {
        _state.value.chats.firstOrNull { it.kind == ChatKind.Direct && it.members.firstOrNull() == contactId }?.let { return it.id }
        val contact = _state.value.contacts[contactId]
        val chat = Chat(newId("chat"), ChatKind.Direct, contact?.name ?: "Chat", listOf(contactId), emptyList(), 0, false, false, now)
        set { it.copy(chats = listOf(chat) + it.chats) }
        return chat.id
    }

    /** Adds, switches or removes my reaction on a message; saved with the chat. */
    fun reactToMessage(chatId: String, messageId: String, emoji: String) {
        val chat = chatById(chatId) ?: return
        replaceChat(chat.copy(messages = chat.messages.map { if (it.id == messageId) toggleReaction(it, emoji) else it }))
    }

    fun togglePin(chatId: String) {
        chatById(chatId)?.let { replaceChat(it.copy(pinned = !it.pinned)) }
    }

    fun updateProfile(name: String? = null, about: String? = null) {
        set { it.copy(profile = Profile(name ?: it.profile.name, about ?: it.profile.about)) }
    }

    /** Back to the sample conversations (Settings → Reset chats). The profile is kept, like the RN app. */
    suspend fun resetChats(now: Long = clock()) {
        storage.removeItem(STORAGE_KEY)
        val seed = buildSeed(now)
        set { it.copy(contacts = seed.contacts, chats = seed.chats, calls = seed.calls, hydrated = true) }
    }
}
