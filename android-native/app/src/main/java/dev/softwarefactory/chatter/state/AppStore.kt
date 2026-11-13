package dev.softwarefactory.chatter.state

import dev.softwarefactory.chatter.data.KeyValueStorage
import dev.softwarefactory.chatter.domain.Call
import dev.softwarefactory.chatter.domain.Chat
import dev.softwarefactory.chatter.domain.ChatKind
import dev.softwarefactory.chatter.domain.Contact
import dev.softwarefactory.chatter.domain.Message
import dev.softwarefactory.chatter.domain.MessageStatus
import dev.softwarefactory.chatter.domain.advanceStatus
import dev.softwarefactory.chatter.domain.buildSeed
import dev.softwarefactory.chatter.domain.draftMessage
import dev.softwarefactory.chatter.domain.withMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val STORAGE_KEY = "chatter:v1"

@Serializable
data class Profile(val name: String, val about: String)

data class AppState(
    val hydrated: Boolean = false,
    val profile: Profile = Profile("You", "Available"),
    val contacts: Map<String, Contact> = emptyMap(),
    val chats: List<Chat> = emptyList(),
    val calls: List<Call> = emptyList(),
)

/** What is written under [STORAGE_KEY]: the same JSON shape the React Native app saves (no `hydrated`). */
@Serializable
internal data class Persisted(
    val profile: Profile,
    val contacts: Map<String, Contact>,
    val chats: List<Chat>,
    val calls: List<Call>,
)

internal val StoreJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

val REPLIES = listOf("👍", "Sounds good!", "Haha 😄", "On it", "Talk later?", "Nice!", "Sure thing")

/**
 * App state: contacts, chats and calls, persisted to local storage. Port of src/state/store.ts.
 *
 * Observe [state]; call the actions below to update it. Storage, clock and timers are injected so the
 * store can be tested on the JVM. [saveScope] should run saves in order (a single-threaded dispatcher).
 * There is no server: replies to your messages are simulated on the device.
 * Call all actions from one thread (the main thread in the app).
 */
class AppStore(
    private val storage: KeyValueStorage,
    private val clock: Clock = Clock.System,
    private val scheduler: Scheduler,
    private val saveScope: CoroutineScope,
) {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> get() = _state

    private var counter = 0

    private fun set(persist: Boolean = true, update: (AppState) -> AppState) {
        val next = update(_state.value)
        _state.value = next
        if (persist && next.hydrated) save(next)
    }

    private fun save(s: AppState) {
        val json = StoreJson.encodeToString(Persisted.serializer(), Persisted(s.profile, s.contacts, s.chats, s.calls))
        saveScope.launch { storage.setItem(STORAGE_KEY, json) }
    }

    /** Loads what was saved, or the sample conversations on a first launch. */
    suspend fun hydrate(now: Long = clock.now()) {
        val raw = storage.getItem(STORAGE_KEY)
        val saved = raw?.let { StoreJson.decodeFromString(Persisted.serializer(), it) }
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

    private fun newId(prefix: String) = "$prefix-${clock.now()}-${++counter}"

    /** Sends my message; its ticks move on, and in a direct chat the other person may answer. */
    fun sendMessage(chatId: String, text: String, now: Long = clock.now()) {
        val chat = chatById(chatId) ?: return
        val message = draftMessage(text, now, newId("m"))
        replaceChat(withMessage(chat, message))
        val step = {
            chatById(chatId)?.let { current ->
                replaceChat(
                    current.copy(
                        messages = current.messages.map {
                            if (it.id == message.id) it.copy(status = advanceStatus(it.status)) else it
                        },
                    ),
                )
            }
            Unit
        }
        scheduler.schedule(400, step)
        scheduler.schedule(1200, step)
        scheduler.schedule(2600, step)
        if (chat.kind == ChatKind.Direct) {
            scheduler.schedule(4200) {
                val current = chatById(chatId) ?: return@schedule
                val reply = Message(
                    id = newId("r"),
                    from = chat.members[0],
                    text = REPLIES[current.messages.size % REPLIES.size],
                    sentAt = clock.now(),
                    status = MessageStatus.Read,
                )
                replaceChat(current.copy(messages = current.messages + reply))
            }
        }
    }

    /** Opens the direct chat with a contact, starting one if there is none yet. Returns its id. */
    fun startChat(contactId: String, now: Long = clock.now()): String {
        val s = _state.value
        s.chats.firstOrNull { it.kind == ChatKind.Direct && it.members.firstOrNull() == contactId }?.let { return it.id }
        val chat = Chat(
            id = newId("chat"), kind = ChatKind.Direct, title = s.contacts[contactId]?.name ?: "Chat",
            members = listOf(contactId), messages = emptyList(), unread = 0, pinned = false, muted = false, createdAt = now,
        )
        set { it.copy(chats = listOf(chat) + it.chats) }
        return chat.id
    }

    fun togglePin(chatId: String) {
        chatById(chatId)?.let { replaceChat(it.copy(pinned = !it.pinned)) }
    }

    fun updateProfile(name: String? = null, about: String? = null) {
        set { it.copy(profile = Profile(name ?: it.profile.name, about ?: it.profile.about)) }
    }

    /** Back to the sample conversations (Settings → Reset chats). */
    suspend fun resetChats(now: Long = clock.now()) {
        storage.removeItem(STORAGE_KEY)
        val seed = buildSeed(now)
        set { it.copy(contacts = seed.contacts, chats = seed.chats, calls = seed.calls, hydrated = true) }
    }
}
