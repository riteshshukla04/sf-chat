package dev.softwarefactory.chatter.ui

import dev.softwarefactory.chatter.domain.MessageStatus
import dev.softwarefactory.chatter.domain.buildSeed
import dev.softwarefactory.chatter.ui.components.AvatarColours
import dev.softwarefactory.chatter.ui.components.avatarColour
import dev.softwarefactory.chatter.ui.screens.chatRowModel
import dev.softwarefactory.chatter.ui.screens.emptyMessage
import dev.softwarefactory.chatter.ui.screens.sortedContacts
import dev.softwarefactory.chatter.ui.screens.visibleChats
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatRowModelTest {
    private val now = 1_800_000_000_000
    private val zone = TimeZone.getTimeZone("UTC")
    private val seed = buildSeed(now)

    @Test fun pinnedChatFirstThenMostRecent() {
        val ids = visibleChats(seed.chats, "").map { it.id }
        assertEquals("family", ids.first())
        assertEquals(seed.chats.size, ids.size)
    }

    @Test fun searchFiltersByTitle() {
        assertEquals(listOf("asha"), visibleChats(seed.chats, " asha ").map { it.id }.filter { it == "asha" })
        assertTrue(visibleChats(seed.chats, "zzzz-nothing").isEmpty())
        assertEquals("No chats match “zz”", emptyMessage("zz"))
    }

    @Test fun rowModelFlags() {
        val family = chatRowModel(seed.chats.first { it.id == "family" }, seed.contacts, now, zone)
        assertTrue(family.pinned); assertTrue(family.group); assertTrue(family.timeHighlighted)
        assertEquals(1, family.unread)
        val flat = chatRowModel(seed.chats.first { it.id == "flat" }, seed.contacts, now, zone)
        assertTrue(flat.muted); assertFalse(flat.timeHighlighted); assertEquals(4, flat.unread)
    }

    @Test fun statusPrefixForOwnLastMessage() {
        val base = seed.chats.first { it.id == "ben" }
        fun prefix(status: MessageStatus?, from: String = "me"): String {
            val msgs = if (status == null) emptyList() else listOf(base.messages.last().copy(from = from, status = status))
            return chatRowModel(base.copy(messages = msgs), seed.contacts, now, zone).statusPrefix
        }
        assertEquals("✓✓ ", prefix(MessageStatus.Read))
        assertEquals("🕓 ", prefix(MessageStatus.Sending))
        assertEquals("✓ ", prefix(MessageStatus.Sent))
        assertEquals("", prefix(MessageStatus.Read, from = "ben"))
        assertEquals("", prefix(null))
    }

    @Test fun contactsSortedByName() {
        val names = sortedContacts(seed.contacts).map { it.name }
        assertEquals(names.sortedBy { it.lowercase() }, names)
    }

    @Test fun avatarColourIsStableAndFromPalette() {
        assertEquals(avatarColour("Asha Rao"), avatarColour("Asha Rao"))
        // "A" = 65 -> 65 % 8 = 1 -> #3E8EDE
        assertEquals(androidx.compose.ui.graphics.Color(AvatarColours[1]), avatarColour("A"))
        // "ab" = 97*31+98 = 3105 -> %8 = 1
        assertEquals(androidx.compose.ui.graphics.Color(AvatarColours[1]), avatarColour("ab"))
        assertEquals(androidx.compose.ui.graphics.Color(AvatarColours[0]), avatarColour(""))
    }
}
