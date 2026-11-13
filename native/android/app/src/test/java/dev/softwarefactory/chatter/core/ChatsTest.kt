package dev.softwarefactory.chatter.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/** Port of src/lib/__tests__/chats.test.ts, in a fixed zone so results don't depend on the machine. */
class ChatsTest {
    private val zone = TimeZone.getTimeZone("Europe/Berlin")
    private fun at(day: Int, hour: Int, minute: Int = 0, month: Int = 10): Long =
        Calendar.getInstance(zone).apply { clear(); set(2026, month, day, hour, minute) }.timeInMillis

    // Thursday 12 November 2026, 15:30 local time.
    private val now = at(12, 15, 30)

    private val contacts = mapOf(
        "asha" to Contact("asha", "Asha Rao", "", null),
        "ben" to Contact("ben", "Ben Okafor", "", at(12, 9, 5)),
        "chen" to Contact("chen", "Chen Wei", "", at(9, 20)),
    )

    private fun chat(id: String, kind: ChatKind = ChatKind.Direct, title: String = id, members: List<String> = listOf(id),
                     messages: List<Message> = emptyList(), unread: Int = 0, pinned: Boolean = false, muted: Boolean = false) =
        Chat(id, kind, title, members, messages, unread, pinned, muted, at(1, 9))

    private fun msg(id: String, from: String, text: String, sentAt: Long) = Message(id, from, text, sentAt, MessageStatus.Read)

    @Test fun formatTimeTodayYesterdayWeekdayDate() {
        assertEquals("09:07", formatTime(at(12, 9, 7), now, zone))
        assertEquals("Yesterday", formatTime(at(11, 23, 59), now, zone))
        assertEquals("Monday", formatTime(at(9, 12), now, zone))
        assertEquals("02/11/2026", formatTime(at(2, 12), now, zone))
    }

    @Test fun dayHeadersSayTodayInsteadOfAClock() {
        assertEquals("Today", dayLabel(at(12, 8), now, zone))
        assertEquals("Yesterday", dayLabel(at(11, 8), now, zone))
    }

    @Test fun formatClockIsHoursAndMinutes() {
        assertEquals("09:07", formatClock(at(12, 9, 7), zone))
    }

    @Test fun pinnedFirstThenMostRecentActivity() {
        val old = chat("old", messages = listOf(msg("1", "ben", "hi", at(10, 9))))
        val recent = chat("recent", messages = listOf(msg("2", "asha", "hey", at(12, 14))))
        val pinned = chat("pinned", pinned = true, messages = listOf(msg("3", "chen", "yo", at(3, 9))))
        assertEquals(listOf("pinned", "recent", "old"), sortChats(listOf(old, recent, pinned)).map { it.id })
    }

    @Test fun lastActivityFallsBackToCreatedAt() {
        assertEquals(at(1, 9), lastActivityAt(chat("a")))
    }

    @Test fun previewsSayWhoWroteTheLastMessage() {
        assertEquals("You: On my way", previewText(chat("a", messages = listOf(msg("1", "me", "On my way", now))), contacts))
        assertEquals("Ben: Pizza?", previewText(chat("g", ChatKind.Group, members = listOf("asha", "ben"), messages = listOf(msg("1", "ben", "Pizza?", now))), contacts))
        assertEquals("Sure", previewText(chat("b", messages = listOf(msg("1", "ben", "Sure", now))), contacts))
        assertEquals("No messages yet", previewText(chat("new"), contacts))
        assertEquals("Group created. Say hi!", previewText(chat("g", ChatKind.Group), contacts))
    }

    @Test fun unreadBadgeLeavesMutedChatsOut() {
        assertEquals(3, totalUnread(listOf(chat("a", unread = 2), chat("b", unread = 3, muted = true), chat("c", unread = 1))))
    }

    @Test fun searchMatchesTitlesAndMessageText() {
        val chats = listOf(chat("Asha Rao", title = "Asha Rao"), chat("Ben", title = "Ben", messages = listOf(msg("1", "ben", "Movie tonight?", now))))
        assertEquals(listOf("Asha Rao"), searchChats(chats, "asha").map { it.id })
        assertEquals(listOf("Ben"), searchChats(chats, "MOVIE").map { it.id })
        assertEquals(2, searchChats(chats, "  ").size)
    }

    @Test fun directChatSaysWhenContactWasLastSeen() {
        assertEquals("online", headerSubtitle(chat("asha"), contacts, now, zone))
        assertEquals("last seen today at 09:05", headerSubtitle(chat("ben"), contacts, now, zone))
        assertEquals("last seen monday", headerSubtitle(chat("chen"), contacts, now, zone))
    }

    @Test fun groupListsMembersAndLastMessage() {
        val group = chat("g", ChatKind.Group, "Flat 4B", listOf("asha", "ben"), listOf(msg("1", "asha", "Rent paid", at(12, 10, 15))))
        assertEquals("Asha, Ben, You · last message 10:15", headerSubtitle(group, contacts, now, zone))
    }

    @Test fun groupWithNoMessagesStillHasASubtitle() {
        val group = chat("g", ChatKind.Group, "Weekend trip", listOf("asha", "ben"))
        assertEquals("Asha, Ben, You", headerSubtitle(group, contacts, now, zone))
    }

    @Test fun sendingOnlyRealTextAndChatCountsAsRead() {
        assertFalse(canSend("   "))
        assertTrue(canSend(" hi "))
        assertTrue(canSend("x".repeat(4000)))
        assertFalse(canSend("x".repeat(4001)))
        val sent = withMessage(chat("a", unread = 4), draftMessage("  Hello  ", now, "m1"))
        assertEquals(listOf(Message("m1", "me", "Hello", now, MessageStatus.Sending)), sent.messages)
        assertEquals(0, sent.unread)
    }

    @Test fun ticksMoveOnOneStepAtATime() {
        assertEquals(MessageStatus.Sent, advanceStatus(MessageStatus.Sending))
        assertEquals(MessageStatus.Delivered, advanceStatus(MessageStatus.Sent))
        assertEquals(MessageStatus.Read, advanceStatus(MessageStatus.Delivered))
        assertEquals(MessageStatus.Read, advanceStatus(MessageStatus.Read))
    }

    @Test fun messagesAreGroupedUnderDayHeaders() {
        val groups = groupByDay(listOf(msg("1", "me", "a", at(11, 9)), msg("2", "ben", "b", at(11, 10)), msg("3", "me", "c", at(12, 9))), now, zone)
        assertEquals(listOf("Yesterday" to 2, "Today" to 1), groups.map { it.day to it.messages.size })
    }

    @Test fun avatarsShowInitials() {
        assertEquals("AR", initials("Asha Rao"))
        assertEquals("WT", initials("Weekend trip ⛺"))
        assertEquals("?", initials(""))
        assertEquals("Asha", firstName("Asha Rao"))
    }

    private val base = msg("m1", "asha", "hi", at(12, 9))

    @Test fun addsAReactionAndSummarisesIt() {
        val m = toggleReaction(base, "❤️")
        assertEquals(listOf(ReactionPill("❤️", 1, true)), reactionSummary(m))
    }

    @Test fun tappingMyOwnReactionAgainRemovesIt() {
        val m = toggleReaction(toggleReaction(base, "👍"), "👍")
        assertNull(m.reactions)
        assertEquals(emptyList<ReactionPill>(), reactionSummary(m))
    }

    @Test fun differentEmojiReplacesMineOthersAreCounted() {
        var m = toggleReaction(base, "👍", "asha")
        m = toggleReaction(m, "👍")
        assertEquals(listOf(ReactionPill("👍", 2, true)), reactionSummary(m))
        m = toggleReaction(m, "🙏")
        assertEquals(listOf(ReactionPill("👍", 1, false), ReactionPill("🙏", 1, true)), reactionSummary(m))
    }

    @Test fun ignoresEmojisOutsideThePickerAndDoesNotMutate() {
        assertSame(base, toggleReaction(base, "🔥"))
        toggleReaction(base, "👍")
        assertNull(base.reactions)
    }
}
