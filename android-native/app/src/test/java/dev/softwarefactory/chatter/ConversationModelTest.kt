package dev.softwarefactory.chatter

import dev.softwarefactory.chatter.domain.Chat
import dev.softwarefactory.chatter.domain.ChatKind
import dev.softwarefactory.chatter.domain.Contact
import dev.softwarefactory.chatter.domain.Message
import dev.softwarefactory.chatter.domain.MessageStatus
import dev.softwarefactory.chatter.ui.screens.ConversationRow
import dev.softwarefactory.chatter.ui.screens.Tick
import dev.softwarefactory.chatter.ui.screens.avatarColour
import dev.softwarefactory.chatter.ui.screens.conversationRows
import dev.softwarefactory.chatter.ui.screens.tickFor
import dev.softwarefactory.chatter.ui.screens.tickIsRead
import dev.softwarefactory.chatter.ui.screens.tickTag
import java.util.TimeZone
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

class ConversationModelTest {
    private val utc = TimeZone.getTimeZone("UTC")
    private val now = 1_800_000_000_000L // a fixed instant
    private val day = 86_400_000L
    private val contacts = mapOf(
        "asha" to Contact("asha", "Asha Rao", "", null),
        "ben" to Contact("ben", "Ben Lee", "", null),
    )

    private fun msg(id: String, from: String, at: Long, status: MessageStatus = MessageStatus.Read) =
        Message(id, from, "text $id", at, status)

    private fun chat(kind: ChatKind, vararg messages: Message) =
        Chat("c", kind, "Title", listOf("asha", "ben"), messages.toList(), 0, false, false, 0)

    @Test fun rowsAreOldestFirstWithDayHeaders() {
        val rows = conversationRows(chat(ChatKind.Direct, msg("a", "asha", now - 3 * day), msg("b", "me", now - 1000), msg("c", "asha", now - 500)), contacts, now, utc)
        assertEquals(listOf("Day", "Bubble", "Day", "Bubble", "Bubble"), rows.map { it::class.simpleName })
        assertEquals("Today", (rows[2] as ConversationRow.Day).day)
        assertEquals(listOf("a", "b", "c"), rows.filterIsInstance<ConversationRow.Bubble>().map { it.message.id })
    }

    @Test fun mineVersusTheirsAndClock() {
        val rows = conversationRows(chat(ChatKind.Direct, msg("a", "me", 0), msg("b", "asha", 3_600_000L * 14 + 5 * 60_000)), contacts, now, utc)
            .filterIsInstance<ConversationRow.Bubble>()
        assertTrue(rows[0].mine)
        assertFalse(rows[1].mine)
        assertEquals("00:00", rows[0].clock)
        assertEquals("14:05", rows[1].clock)
    }

    @Test fun senderNamesOnlyForOthersInGroups() {
        val g = conversationRows(chat(ChatKind.Group, msg("a", "me", now), msg("b", "ben", now), msg("c", "ghost", now)), contacts, now, utc)
            .filterIsInstance<ConversationRow.Bubble>()
        assertNull(g[0].sender)
        assertEquals("Ben", g[1].sender)
        assertEquals("Someone", g[2].sender)
        val d = conversationRows(chat(ChatKind.Direct, msg("b", "ben", now)), contacts, now, utc).filterIsInstance<ConversationRow.Bubble>()
        assertNull(d[0].sender)
    }

    @Test fun emptyChatHasNoRows() {
        assertTrue(conversationRows(chat(ChatKind.Group), contacts, now, utc).isEmpty())
    }

    @Test fun ticksPerStatus() {
        assertEquals(Tick.Clock, tickFor(MessageStatus.Sending))
        assertEquals(Tick.Single, tickFor(MessageStatus.Sent))
        assertEquals(Tick.Double, tickFor(MessageStatus.Delivered))
        assertEquals(Tick.Double, tickFor(MessageStatus.Read))
        assertTrue(tickIsRead(MessageStatus.Read))
        assertFalse(tickIsRead(MessageStatus.Delivered))
        assertEquals("ticks-delivered", tickTag(MessageStatus.Delivered))
    }

    @Test fun avatarColourIsStableAndMatchesTheRnHash() {
        assertEquals(avatarColour("Asha Rao"), avatarColour("Asha Rao"))
        // "A": 65 % 8 = 1 -> 0x3E8EDE ; "AB": (65*31+66) = 2081 % 8 = 1
        assertEquals(0x3E8EDE, avatarColour("A").toInt())
        assertEquals(0x3E8EDE, avatarColour("AB").toInt())
        assertEquals(0x25A36F, avatarColour("").toInt())
    }
}
