package dev.softwarefactory.chatter.state

import dev.softwarefactory.chatter.core.ChatKind
import dev.softwarefactory.chatter.core.MessageStatus
import dev.softwarefactory.chatter.core.totalUnread
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppStoreTest {
    private val t0 = 1_800_000_000_000L
    private fun TestScope.store(storage: KeyValueStorage = InMemoryStorage()) =
        AppStore(storage, backgroundScope) { t0 + currentTime }

    @Test fun hydrateSeedsOnFirstLaunchAndSaves() = runTest {
        val storage = InMemoryStorage()
        val s = store(storage)
        assertFalse(s.state.value.hydrated)
        s.hydrate()
        runCurrent()
        assertTrue(s.state.value.hydrated)
        assertEquals(6, s.state.value.chats.size)
        assertEquals(8, s.state.value.contacts.size)
        assertEquals(4, s.state.value.calls.size)
        assertEquals(3, totalUnread(s.state.value.chats)) // 1 + 2, flat is muted
        assertNotNull(storage.items[STORAGE_KEY])
    }

    @Test fun hydrateReloadsSavedData() = runTest {
        val storage = InMemoryStorage()
        val a = store(storage)
        a.hydrate()
        a.togglePin("asha")
        a.updateProfile(name = "Me")
        runCurrent()
        val b = store(storage)
        b.hydrate()
        assertTrue(b.chatById("asha")!!.pinned)
        assertEquals("Me", b.state.value.profile.name)
        assertEquals(a.state.value.chats, b.state.value.chats)
    }

    @Test fun storedJsonHasTheReactNativeShape() = runTest {
        val storage = InMemoryStorage()
        val s = store(storage)
        s.hydrate()
        runCurrent()
        val root = Json.parseToJsonElement(storage.items[STORAGE_KEY]!!).jsonObject
        assertEquals(setOf("profile", "contacts", "chats", "calls"), root.keys)
        assertEquals(setOf("name", "about"), root["profile"]!!.jsonObject.keys)
        val asha = root["contacts"]!!.jsonObject["asha"]!!.jsonObject
        assertEquals(JsonNull, asha["lastSeenAt"]) // null is written, not omitted
        val chat = root["chats"]!!.jsonArray.map { it as JsonObject }.first { it["id"]!!.toString() == "\"family\"" }
        assertEquals("\"group\"", chat["kind"].toString())
        val msg = chat["messages"] as JsonArray
        assertFalse(msg[0].jsonObject.containsKey("reactions")) // optional, absent
        assertEquals("\"read\"", msg[0].jsonObject["status"].toString())
        assertEquals("\"missed\"", root["calls"]!!.jsonArray[0].jsonObject["direction"].toString())
    }

    @Test fun readsRnJsonWithMissingOptionalFields() = runTest {
        val storage = InMemoryStorage(mutableMapOf(STORAGE_KEY to """
            {"profile":{"name":"Ritesh","about":"Hi"},
             "contacts":{"x":{"id":"x","name":"Xi Ping","about":"a","lastSeenAt":null},"y":{"id":"y","name":"Yan","about":"b"}},
             "chats":[{"id":"c","kind":"direct","title":"Xi Ping","members":["x"],"unread":0,"pinned":false,"muted":false,"createdAt":5,
               "messages":[{"id":"m","from":"x","text":"yo","sentAt":6,"status":"sent","reactions":{"👍":["me"]}}]}],
             "calls":[]}""".trimIndent()))
        val s = store(storage)
        s.hydrate()
        assertEquals("Ritesh", s.state.value.profile.name)
        assertNull(s.state.value.contacts["y"]!!.lastSeenAt)
        assertEquals(listOf("me"), s.chatById("c")!!.messages[0].reactions!!["👍"])
        runCurrent()
        val saved = Json.parseToJsonElement(storage.items[STORAGE_KEY]!!).jsonObject["chats"]!!.jsonArray[0].jsonObject["messages"]!!.jsonArray[0].jsonObject
        assertEquals("""{"👍":["me"]}""", saved["reactions"].toString())
    }

    @Test fun sendAdvancesTicksAndDirectChatGetsAReply() = runTest {
        val s = store()
        s.hydrate()
        val before = s.chatById("ben")!!.messages.size
        s.sendMessage("ben", "  hello ")
        fun mine() = s.chatById("ben")!!.messages[before]
        assertEquals("hello", mine().text)
        assertEquals(MessageStatus.Sending, mine().status)
        advanceTimeBy(399); runCurrent()
        assertEquals(MessageStatus.Sending, mine().status)
        advanceTimeBy(1); runCurrent()
        assertEquals(MessageStatus.Sent, mine().status)
        advanceTimeBy(800); runCurrent() // 1200
        assertEquals(MessageStatus.Delivered, mine().status)
        advanceTimeBy(1400); runCurrent() // 2600
        assertEquals(MessageStatus.Read, mine().status)
        assertEquals(before + 1, s.chatById("ben")!!.messages.size)
        advanceTimeBy(1599); runCurrent()
        assertEquals(before + 1, s.chatById("ben")!!.messages.size)
        advanceTimeBy(1); runCurrent() // 4200
        val msgs = s.chatById("ben")!!.messages
        assertEquals(before + 2, msgs.size)
        val reply = msgs.last()
        assertEquals("ben", reply.from)
        assertEquals(REPLIES[(before + 1) % REPLIES.size], reply.text)
        assertEquals(MessageStatus.Read, reply.status)
    }

    @Test fun groupChatsGetNoReply() = runTest {
        val s = store()
        s.hydrate()
        val before = s.chatById("flat")!!.messages.size
        s.sendMessage("flat", "hi all")
        advanceTimeBy(10_000); runCurrent()
        assertEquals(before + 1, s.chatById("flat")!!.messages.size)
        assertEquals(0, s.chatById("flat")!!.unread)
    }

    @Test fun startChatReusesAnExistingDirectChat() = runTest {
        val s = store()
        s.hydrate()
        val n = s.state.value.chats.size
        assertEquals("asha", s.startChat("asha"))
        assertEquals(n, s.state.value.chats.size)
        val id = s.startChat("dana")
        assertEquals(n + 1, s.state.value.chats.size)
        assertEquals(id, s.state.value.chats.first().id)
        assertEquals(ChatKind.Direct, s.chatById(id)!!.kind)
        assertEquals("Dana Silva", s.chatById(id)!!.title)
        assertEquals(id, s.startChat("dana"))
    }

    @Test fun markReadReactAndPin() = runTest {
        val s = store()
        s.hydrate()
        s.markRead("asha")
        assertEquals(0, s.chatById("asha")!!.unread)
        val m = s.chatById("asha")!!.messages[0].id
        s.reactToMessage("asha", m, "😂")
        assertEquals(listOf("me"), s.chatById("asha")!!.messages[0].reactions!!["😂"])
        s.reactToMessage("asha", m, "😂")
        assertNull(s.chatById("asha")!!.messages[0].reactions)
        s.togglePin("asha"); assertTrue(s.chatById("asha")!!.pinned)
        s.togglePin("asha"); assertFalse(s.chatById("asha")!!.pinned)
    }

    @Test fun resetGoesBackToTheSeedAndKeepsTheProfile() = runTest {
        val storage = InMemoryStorage()
        val s = store(storage)
        s.hydrate()
        s.updateProfile(name = "Me")
        s.sendMessage("ben", "x")
        s.startChat("dana")
        s.resetChats()
        runCurrent()
        assertEquals(6, s.state.value.chats.size)
        assertEquals(2, s.chatById("ben")!!.messages.size)
        assertEquals("Me", s.state.value.profile.name)
        assertTrue(s.state.value.hydrated)
        assertEquals(6, Json.parseToJsonElement(storage.items[STORAGE_KEY]!!).jsonObject["chats"]!!.jsonArray.size)
    }

    @Test fun nothingIsSavedBeforeHydrated() = runTest {
        val storage = InMemoryStorage()
        val s = store(storage)
        s.updateProfile(name = "Early")
        runCurrent()
        assertTrue(storage.items.isEmpty())
    }
}
