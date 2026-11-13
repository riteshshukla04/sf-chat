package dev.softwarefactory.chatter.ui.screens

import dev.softwarefactory.chatter.domain.Chat
import dev.softwarefactory.chatter.domain.ChatKind
import dev.softwarefactory.chatter.domain.Contact
import dev.softwarefactory.chatter.domain.Message
import dev.softwarefactory.chatter.domain.MessageStatus
import dev.softwarefactory.chatter.domain.firstName
import dev.softwarefactory.chatter.domain.groupByDay
import dev.softwarefactory.chatter.domain.formatClock
import java.util.TimeZone

/** One line of the conversation list. Presentation logic of src/app/chat/[id].tsx, kept free of Compose so it can be unit tested. */
sealed interface ConversationRow {
    val key: String

    data class Day(val day: String) : ConversationRow {
        override val key get() = "day-$day"
    }

    data class Bubble(
        val message: Message,
        val mine: Boolean,
        /** Sender name above the text: only for other people's messages in a group. */
        val sender: String?,
        val clock: String,
    ) : ConversationRow {
        override val key get() = message.id
    }
}

/** Day headers and bubbles, oldest first (the screen lays them out from the bottom up, so the newest is at the bottom). */
fun conversationRows(chat: Chat, contacts: Map<String, Contact>, now: Long, zone: TimeZone = TimeZone.getDefault()): List<ConversationRow> {
    val out = mutableListOf<ConversationRow>()
    for (group in groupByDay(chat.messages, now, zone)) {
        out += ConversationRow.Day(group.day)
        for (m in group.messages) {
            val mine = m.from == "me"
            out += ConversationRow.Bubble(
                message = m,
                mine = mine,
                sender = if (chat.kind == ChatKind.Group && !mine) firstName(contacts[m.from]?.name ?: "Someone") else null,
                clock = formatClock(m.sentAt, zone),
            )
        }
    }
    return out
}

/** Which tick glyph a status gets (RN: clock-outline / check / check-all). */
enum class Tick { Clock, Single, Double }

fun tickFor(status: MessageStatus): Tick = when (status) {
    MessageStatus.Sending -> Tick.Clock
    MessageStatus.Sent -> Tick.Single
    else -> Tick.Double
}

/** Ticks are blue only once the message is read. */
fun tickIsRead(status: MessageStatus) = status == MessageStatus.Read

/** testID suffix of the tick icon, same as RN (`ticks-sending`, `ticks-sent`, `ticks-delivered`, `ticks-read`). */
fun tickTag(status: MessageStatus) = "ticks-${status.name.lowercase()}"

private val AVATAR_COLOURS = longArrayOf(0x25A36F, 0x3E8EDE, 0xD9822B, 0x9B59B6, 0xE0566E, 0x16A2B8, 0x7A8B2E, 0xC0612B)

/** Stable avatar colour per name (RGB, port of colourFor in src/components/avatar.tsx). */
fun avatarColour(name: String): Long {
    var hash = 0L
    for (ch in name) hash = (hash * 31 + ch.code) and 0xFFFFFFFFL
    return AVATAR_COLOURS[(hash % AVATAR_COLOURS.size).toInt()]
}
