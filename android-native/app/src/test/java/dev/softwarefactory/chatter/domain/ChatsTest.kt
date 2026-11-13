package dev.softwarefactory.chatter.domain

import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

/** Port of src/lib/__tests__/chats.test.ts. */
class ChatsTest {
    private val zone: TimeZone = TimeZone.getTimeZone("UTC")

    private fun at(day: Int, hour: Int, minute: Int = 0): Long =
        Calendar.getInstance(zone).apply { clear(); set(2026, Calendar.NOVEMBER, day, hour, minute) }.timeInMillis

    // Thursday 12 November 2026, 15:30.
    private val now = at(12, 15, 30)

    private val contacts = mapOf(
        "asha" to Contact("asha", "Asha Rao", "", null),
        "ben" to Contact("ben", "Ben Okafor", "", at(12, 9, 5)),
        "chen" to Contact("chen", "Chen Wei", "", at(9, 20)),
    )

    private fun chat(
        id: String,
        kind: ChatKind = ChatKind.Direct,
        title: String = id,
        members: List<String> = listOf(id),
        messages: List<Message> = emptyList(),
        unread: Int = 0,
        pinned: Boolean = false,
        muted: Boolean = false,
    ) = Chat(id, kind, title, members, messages, unread, pinned, muted, at(1, 9))

    private fun msg(id: String, from: String, text: String, sentAt: Long) = Message(id, from, text, sentAt, MessageStatus.Read)

    @Test fun formatTimeShowsClockYesterdayWeekdayAndDate() {
        assertEquals("09:07", formatTime(at(12, 9, 7), now, zone))
        assertEquals("Yesterday", formatTime(at(11, 23, 59), now, zone))
        assertEquals("Monday", formatTime(at(9, 12), now, zone))
        assertEquals("02/11/2026", formatTime(at(2, 12), now, zone))
    }

    @Test fun dayHeadersSayTodayInsteadOfAClock() {
        assertEquals("Today", dayLabel(at(12, 8), now, zone))
        assertEquals("Yesterday", dayLabel(at(11, 8), now, zone))
    }

    @Test fun formatClockIsHourAndMinute() {
        assertEquals("09:05", formatClock(at(12, 9, 5), zone))
    }

    @Test fun chatListIsPinnedFirstThenMostRecentActivity() {
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
        assertEquals(
            "Ben: Pizza?",
            previewText(chat("g", kind = ChatKind.Group, members = listOf("asha", "ben"), messages = listOf(msg("1", "ben", "Pizza?", now))), contacts),
        )
        assertEquals("Sure", previewText(chat("b", messages = listOf(msg("1", "ben", "Sure", now))), contacts))
        assertEquals("No messages yet", previewText(chat("new"), contacts))
        assertEquals("Group created. Say hi!", previewText(chat("g", kind = ChatKind.Group), contacts))
        assertEquals(
            "Someone: hi",
            previewText(chat("g", kind = ChatKind.Group, messages = listOf(msg("1", "zed", "hi", now))), contacts),
        )
    }

    @Test fun unreadBadgeLeavesMutedChatsOut() {
        assertEquals(3, totalUnread(listOf(chat("a", unread = 2), chat("b", unread = 3, muted = true), chat("c", unread = 1))))
    }

    @Test fun searchMatchesTitlesAndMessageText() {
        val chats = listOf(
            chat("Asha Rao", title = "Asha Rao"),
            chat("Ben", title = "Ben", messages = listOf(msg("1", "ben", "Movie tonight?", now))),
        )
        assertEquals(listOf("Asha Rao"), searchChats(chats, "asha").map { it.id })
        assertEquals(listOf("Ben"), searchChats(chats, "MOVIE").map { it.id })
        assertEquals(2, searchChats(chats, "  ").size)
    }

    @Test fun directChatSaysWhenTheContactWasLastSeen() {
        assertEquals("online", headerSubtitle(chat("asha"), contacts, now, zone))
        assertEquals("last seen today at 09:05", headerSubtitle(chat("ben"), contacts, now, zone))
        assertEquals("last seen monday", headerSubtitle(chat("chen"), contacts, now, zone))
        assertEquals("last seen just now", headerSubtitle(chat("asha"), mapOf("asha" to Contact("asha", "A", "", now - 30_000)), now, zone))
        assertEquals("", headerSubtitle(chat("nobody"), contacts, now, zone))
    }

    @Test fun groupListsMembersAndLastMessage() {
        val group = chat("g", kind = ChatKind.Group, title = "Flat 4B", members = listOf("asha", "ben"), messages = listOf(msg("1", "asha", "Rent paid", at(12, 10, 15))))
        assertEquals("Asha, Ben, You · last message 10:15", headerSubtitle(group, contacts, now, zone))
    }

    @Test fun groupWithoutMessagesDoesNotCrash() {
        val group = chat("g", kind = ChatKind.Group, members = listOf("asha", "ben"))
        assertEquals("Asha, Ben, You", headerSubtitle(group, contacts, now, zone))
    }

    @Test fun sendingNeedsRealTextAndMarksTheChatRead() {
        assertEquals(false, canSend("   "))
        assertEquals(true, canSend(" hi "))
        assertEquals(false, canSend("x".repeat(4001)))
        assertEquals(true, canSend("x".repeat(4000)))
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
        val groups = groupByDay(
            listOf(msg("1", "me", "a", at(11, 9)), msg("2", "ben", "b", at(11, 10)), msg("3", "me", "c", at(12, 9))),
            now, zone,
        )
        assertEquals(listOf("Yesterday" to 2, "Today" to 1), groups.map { it.day to it.messages.size })
    }

    @Test fun avatarsShowInitials() {
        assertEquals("AR", initials("Asha Rao"))
        assertEquals("WT", initials("Weekend trip ⛺"))
        assertEquals("A", initials("  asha "))
        assertEquals("?", initials(""))
    }

    @Test fun firstNameIsTheFirstWord() {
        assertEquals("Asha", firstName("Asha Rao"))
        assertEquals("Cher", firstName("Cher"))
    }

    @Test fun seedMatchesTheOriginalSampleData() {
        val seed = buildSeed(now)
        assertEquals(8, seed.contacts.size)
        assertEquals(listOf("family", "asha", "weekend", "ben", "flat", "chen"), seed.chats.map { it.id })
        assertEquals(listOf("c1", "c2", "c3", "c4"), seed.calls.map { it.id })
        assertEquals(3, totalUnread(seed.chats)) // flat (4) is muted: 1 + 2
    }
}
