package dev.softwarefactory.chatter.ui

import dev.softwarefactory.chatter.core.Chat
import dev.softwarefactory.chatter.core.ChatKind
import dev.softwarefactory.chatter.core.Contact
import dev.softwarefactory.chatter.core.Message
import dev.softwarefactory.chatter.core.MessageStatus
import dev.softwarefactory.chatter.ui.screens.chatRowModel
import dev.softwarefactory.chatter.ui.screens.emptyChatsMessage
import dev.softwarefactory.chatter.ui.screens.sortedContacts
import dev.softwarefactory.chatter.ui.screens.tickPrefix
import dev.softwarefactory.chatter.ui.screens.visibleChats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

class ChatRowsTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val now = 1_700_000_000_000L
    private val contacts = mapOf("a" to Contact("a", "Asha Rao", "hi"), "b" to Contact("b", "ben"), "c" to Contact("c", "Çem"))

    private fun msg(id: String, from: String, status: MessageStatus, at: Long = now - 60_000, text: String = "t$id") =
        Message(id, from, text, at, status)

    private fun chat(id: String, vararg m: Message, pinned: Boolean = false, muted: Boolean = false, unread: Int = 0, title: String = id) =
        Chat(id, ChatKind.Direct, title, listOf("a"), m.toList(), unread, pinned, muted, 0)

    @Test fun tickPrefixByStatusOfMyLastMessage() {
        assertEquals("🕓 ", tickPrefix(chat("x", msg("1", "me", MessageStatus.Sending))))
        assertEquals("✓ ", tickPrefix(chat("x", msg("1", "me", MessageStatus.Sent))))
        assertEquals("✓ ", tickPrefix(chat("x", msg("1", "me", MessageStatus.Delivered))))
        assertEquals("✓✓ ", tickPrefix(chat("x", msg("1", "me", MessageStatus.Read))))
    }

    @Test fun noTickForTheirMessagesOrEmptyChats() {
        assertEquals("", tickPrefix(chat("x", msg("1", "a", MessageStatus.Read))))
        assertEquals("", tickPrefix(chat("x")))
    }

    @Test fun rowModelCombinesPrefixAndPreview() {
        val m = chatRowModel(chat("x", msg("1", "me", MessageStatus.Read, text = "yo")), contacts, now, utc)
        assertEquals("✓✓ You: yo", m.subtitle)
    }

    @Test fun unreadStyling() {
        val unread = chatRowModel(chat("x", unread = 3), contacts, now, utc)
        assertTrue(unread.timeAccent); assertFalse(unread.badgeMuted)
        val muted = chatRowModel(chat("x", unread = 3, muted = true), contacts, now, utc)
        assertFalse(muted.timeAccent); assertTrue(muted.badgeMuted)
        assertFalse(chatRowModel(chat("x"), contacts, now, utc).timeAccent)
    }

    @Test fun pinnedFirstThenLatestAndSearchFilters() {
        val old = chat("old", msg("1", "a", MessageStatus.Read, at = 1000), pinned = true)
        val recent = chat("recent", msg("2", "a", MessageStatus.Read, at = 3000))
        val mid = chat("mid", msg("3", "a", MessageStatus.Read, at = 2000, text = "Lunch?"))
        assertEquals(listOf("old", "recent", "mid"), visibleChats(listOf(mid, recent, old), "").map { it.id })
        assertEquals(listOf("mid"), visibleChats(listOf(mid, recent, old), " lunch ").map { it.id })
        assertTrue(visibleChats(listOf(mid), "zzz").isEmpty())
    }

    @Test fun emptyMessageUsesQuery() = assertEquals("No chats match “zz”", emptyChatsMessage("zz"))

    @Test fun contactsSortedByNameIgnoringCase() {
        assertEquals(listOf("a", "b", "c"), sortedContacts(contacts, Locale.US).map { it.id })
    }
}
