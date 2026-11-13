package dev.softwarefactory.chatter.state

import dev.softwarefactory.chatter.data.InMemoryStorage
import dev.softwarefactory.chatter.domain.ChatKind
import dev.softwarefactory.chatter.domain.MessageStatus
import dev.softwarefactory.chatter.domain.buildSeed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStoreTest {
    private val t0 = 1_800_000_000_000L
    private val clock = FakeScheduler(t0)

    private fun store(storage: InMemoryStorage = InMemoryStorage()) =
        AppStore(storage, clock, clock, CoroutineScope(Dispatchers.Unconfined))

    private fun hydrated(storage: InMemoryStorage = InMemoryStorage()): AppStore =
        store(storage).also { runBlocking { it.hydrate(t0) } }

    @Test fun firstLaunchHydratesTheSeedAndSavesIt() {
        val storage = InMemoryStorage()
        val s = hydrated(storage)
        assertTrue(s.state.value.hydrated)
        assertEquals(buildSeed(t0).chats, s.state.value.chats)
        assertEquals(buildSeed(t0).calls, s.state.value.calls)
        assertNotNull(storage.values[STORAGE_KEY])
    }

    @Test fun savedStateWinsOverTheSeed() {
        val storage = InMemoryStorage()
        val first = hydrated(storage)
        first.togglePin("ben")
        first.updateProfile(name = "Sam")
        val second = hydrated(storage)
        assertEquals(true, second.chatById("ben")!!.pinned)
        assertEquals("Sam", second.state.value.profile.name)
        assertEquals(first.state.value.chats, second.state.value.chats)
    }

    @Test fun savesTheSameJsonShapeAsTheReactNativeApp() {
        val storage = InMemoryStorage()
        hydrated(storage)
        val root = Json.parseToJsonElement(storage.values.getValue("chatter:v1")).jsonObject
        assertEquals(setOf("profile", "contacts", "chats", "calls"), root.keys)
        assertEquals("You", root.getValue("profile").jsonObject.getValue("name").jsonPrimitive.content)
        val asha = root.getValue("contacts").jsonObject.getValue("asha").jsonObject
        assertEquals(JsonNull, asha.getValue("lastSeenAt")) // null when online, as in JSON.stringify
        val weekend = root.getValue("chats").jsonArray.map { it.jsonObject }.first { it.getValue("id").jsonPrimitive.content == "weekend" }
        assertEquals("group", weekend.getValue("kind").jsonPrimitive.content)
        assertEquals(JsonArray(emptyList()), weekend.getValue("messages"))
        val call = root.getValue("calls").jsonArray[0].jsonObject
        assertEquals("missed", call.getValue("direction").jsonPrimitive.content)
        assertEquals(setOf("id", "contactId", "at", "direction", "video"), call.keys)
    }

    @Test fun readsStateSavedByTheReactNativeApp() {
        val raw = """{"profile":{"name":"Rn","about":"Hi"},"contacts":{"asha":{"id":"asha","name":"Asha Rao","about":"x","lastSeenAt":null}},
            |"chats":[{"id":"asha","kind":"direct","title":"Asha Rao","members":["asha"],"messages":[{"id":"m","from":"me","text":"yo","sentAt":5,"status":"delivered"}],"unread":0,"pinned":false,"muted":false,"createdAt":1}],
            |"calls":[{"id":"c1","contactId":"asha","at":9,"direction":"incoming","video":true}]}""".trimMargin()
        val s = hydrated(InMemoryStorage(mapOf(STORAGE_KEY to raw)))
        assertEquals("Rn", s.state.value.profile.name)
        assertEquals(MessageStatus.Delivered, s.chatById("asha")!!.messages[0].status)
        assertNull(s.state.value.contacts.getValue("asha").lastSeenAt)
    }

    @Test fun markReadClearsUnread() {
        val s = hydrated()
        assertEquals(2, s.chatById("asha")!!.unread)
        s.markRead("asha")
        assertEquals(0, s.chatById("asha")!!.unread)
    }

    @Test fun sendFlowMovesTicksThenAnswersInADirectChat() {
        val storage = InMemoryStorage()
        val s = hydrated(storage)
        val before = s.chatById("ben")!!.messages.size // 2
        s.sendMessage("ben", "  hello  ")
        fun mine() = s.chatById("ben")!!.messages.last { it.from == "me" }
        assertEquals("hello", mine().text)
        assertEquals(MessageStatus.Sending, mine().status)
        assertEquals(before + 1, s.chatById("ben")!!.messages.size)
        clock.advanceBy(399); assertEquals(MessageStatus.Sending, mine().status)
        clock.advanceBy(1); assertEquals(MessageStatus.Sent, mine().status)
        clock.advanceBy(800); assertEquals(MessageStatus.Delivered, mine().status)
        clock.advanceBy(1400); assertEquals(MessageStatus.Read, mine().status)
        assertEquals(before + 1, s.chatById("ben")!!.messages.size) // no reply yet (2600ms)
        clock.advanceBy(1599); assertEquals(before + 1, s.chatById("ben")!!.messages.size)
        clock.advanceBy(1)
        val msgs = s.chatById("ben")!!.messages
        assertEquals(before + 2, msgs.size)
        assertEquals("ben", msgs.last().from)
        assertEquals(REPLIES[(before + 1) % REPLIES.size], msgs.last().text)
        assertEquals(MessageStatus.Read, msgs.last().status)
        assertTrue(storage.values.getValue(STORAGE_KEY).contains(msgs.last().text))
    }

    @Test fun groupChatsGetNoSimulatedReply() {
        val s = hydrated()
        val before = s.chatById("family")!!.messages.size
        s.sendMessage("family", "hi all")
        assertEquals(0, s.chatById("family")!!.unread)
        clock.advanceBy(10_000)
        assertEquals(before + 1, s.chatById("family")!!.messages.size)
        assertEquals(MessageStatus.Read, s.chatById("family")!!.messages.last().status)
        assertEquals(0, clock.pending)
    }

    @Test fun sendingToAnUnknownChatDoesNothing() {
        val s = hydrated()
        s.sendMessage("nope", "hi")
        assertEquals(0, clock.pending)
    }

    @Test fun startChatReusesAnExistingDirectChat() {
        val s = hydrated()
        val n = s.state.value.chats.size
        assertEquals("asha", s.startChat("asha"))
        assertEquals(n, s.state.value.chats.size)
    }

    @Test fun startChatCreatesANewDirectChatAtTheTop() {
        val s = hydrated()
        val id = s.startChat("eli")
        val chat = s.chatById(id)!!
        assertEquals(s.state.value.chats.first().id, id)
        assertEquals(ChatKind.Direct, chat.kind)
        assertEquals("Eli Novak", chat.title)
        assertEquals(listOf("eli"), chat.members)
        assertEquals(t0, chat.createdAt)
        assertEquals(id, s.startChat("eli"))
    }

    @Test fun togglePinFlipsPinned() {
        val s = hydrated()
        s.togglePin("ben"); assertEquals(true, s.chatById("ben")!!.pinned)
        s.togglePin("ben"); assertEquals(false, s.chatById("ben")!!.pinned)
    }

    @Test fun resetChatsBringsTheSeedBackAndKeepsTheProfile() {
        val storage = InMemoryStorage()
        val s = hydrated(storage)
        s.updateProfile(name = "Sam")
        s.togglePin("ben")
        s.startChat("eli")
        runBlocking { s.resetChats(t0) }
        assertEquals(buildSeed(t0).chats, s.state.value.chats)
        assertEquals("Sam", s.state.value.profile.name)
        assertTrue(s.state.value.hydrated)
        // and it is saved again
        assertEquals(false, hydrated(storage).chatById("ben")!!.pinned)
    }

    @Test fun updateProfileChangesOnlyTheGivenFields() {
        val s = hydrated()
        s.updateProfile(about = "Away")
        assertEquals("You", s.state.value.profile.name)
        assertEquals("Away", s.state.value.profile.about)
    }
}
