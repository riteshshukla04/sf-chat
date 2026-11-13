package dev.softwarefactory.chatter.domain

import java.util.Calendar
import java.util.TimeZone
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Chat logic: plain Kotlin, no Android, so it is easy to test. Port of src/lib/chats.ts.
 * Screens and the store call these; nothing here touches storage or the network.
 */

@Serializable
enum class MessageStatus {
    @SerialName("sending") Sending,
    @SerialName("sent") Sent,
    @SerialName("delivered") Delivered,
    @SerialName("read") Read,
}

@Serializable
data class Message(
    val id: String,
    /** "me" for my own messages, otherwise a contact id. */
    val from: String,
    val text: String,
    /** Epoch milliseconds. */
    val sentAt: Long,
    val status: MessageStatus,
)

@Serializable
data class Contact(
    val id: String,
    val name: String,
    val about: String,
    /** Epoch milliseconds the contact was last online, or null when online now. */
    val lastSeenAt: Long?,
)

@Serializable
enum class ChatKind {
    @SerialName("direct") Direct,
    @SerialName("group") Group,
}

@Serializable
data class Chat(
    val id: String,
    val kind: ChatKind,
    val title: String,
    /** Contact ids (without "me"). */
    val members: List<String>,
    /** Oldest first. */
    val messages: List<Message>,
    /** How many of the newest messages I haven't read. */
    val unread: Int,
    val pinned: Boolean,
    val muted: Boolean,
    val createdAt: Long,
)

private const val MINUTE = 60_000L
private const val DAY = 24 * 60 * MINUTE
private val WEEKDAYS = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

private fun pad(n: Int) = n.toString().padStart(2, '0')

private fun calendarAt(at: Long, zone: TimeZone): Calendar =
    Calendar.getInstance(zone).apply { timeInMillis = at }

/** "14:05" today, "Yesterday", the weekday within a week, else "12/11/2026". */
fun formatTime(at: Long, now: Long, zone: TimeZone = TimeZone.getDefault()): String {
    val time = calendarAt(at, zone)
    val today = calendarAt(now, zone)
    val startOfToday = Calendar.getInstance(zone).apply {
        clear()
        set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
    return when {
        at >= startOfToday -> "${pad(time.get(Calendar.HOUR_OF_DAY))}:${pad(time.get(Calendar.MINUTE))}"
        at >= startOfToday - DAY -> "Yesterday"
        at >= startOfToday - 6 * DAY -> WEEKDAYS[time.get(Calendar.DAY_OF_WEEK) - 1]
        else -> "${pad(time.get(Calendar.DAY_OF_MONTH))}/${pad(time.get(Calendar.MONTH) + 1)}/${time.get(Calendar.YEAR)}"
    }
}

/** Clock time of a message inside a conversation. */
fun formatClock(at: Long, zone: TimeZone = TimeZone.getDefault()): String {
    val time = calendarAt(at, zone)
    return "${pad(time.get(Calendar.HOUR_OF_DAY))}:${pad(time.get(Calendar.MINUTE))}"
}

/** When something last happened in a chat: its newest message, or when it was created. */
fun lastActivityAt(chat: Chat): Long = chat.messages.lastOrNull()?.sentAt ?: chat.createdAt

/** Pinned chats first, then the most recently active. */
fun sortChats(chats: List<Chat>): List<Chat> =
    chats.sortedWith(
        compareByDescending<Chat> { it.pinned }.thenByDescending { lastActivityAt(it) },
    )

/** The line under a chat's name in the list. */
fun previewText(chat: Chat, contacts: Map<String, Contact>): String {
    val last = chat.messages.lastOrNull()
        ?: return if (chat.kind == ChatKind.Group) "Group created. Say hi!" else "No messages yet"
    if (last.from == "me") return "You: ${last.text}"
    if (chat.kind == ChatKind.Group) return "${firstName(contacts[last.from]?.name ?: "Someone")}: ${last.text}"
    return last.text
}

fun firstName(name: String): String = name.split(" ").first()

/** Total unread messages over all chats that aren't muted (the badge on the Chats tab). */
fun totalUnread(chats: List<Chat>): Int = chats.filter { !it.muted }.sumOf { it.unread }

/** Chats whose title, or any message, contains the query (case-insensitive). */
fun searchChats(chats: List<Chat>, query: String): List<Chat> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return chats
    return chats.filter { c -> c.title.lowercase().contains(q) || c.messages.any { it.text.lowercase().contains(q) } }
}

/**
 * The line under the name in a conversation's header.
 *
 * Deliberate difference from the TypeScript original: for a group with no messages yet the original
 * dereferences `last!.sentAt` and throws (the "Weekend trip" seed chat). Here the "last message" part
 * is simply left out.
 */
fun headerSubtitle(chat: Chat, contacts: Map<String, Contact>, now: Long, zone: TimeZone = TimeZone.getDefault()): String {
    if (chat.kind == ChatKind.Group) {
        val names = chat.members.map { firstName(contacts[it]?.name ?: "Someone") }
        val last = chat.messages.lastOrNull() ?: return "${names.joinToString(", ")}, You"
        return "${names.joinToString(", ")}, You · last message ${formatTime(last.sentAt, now, zone)}"
    }
    val contact = chat.members.firstOrNull()?.let { contacts[it] } ?: return ""
    val seen = contact.lastSeenAt ?: return "online"
    if (now - seen < MINUTE) return "last seen just now"
    val label = formatTime(seen, now, zone)
    return if (label.contains(':')) "last seen today at $label" else "last seen ${label.lowercase()}"
}

/** A new message of mine, not yet sent. */
fun draftMessage(text: String, now: Long, id: String): Message =
    Message(id = id, from = "me", text = text.trim(), sentAt = now, status = MessageStatus.Sending)

/** Whether a draft can be sent: something other than spaces, at most 4,000 characters. */
fun canSend(text: String): Boolean {
    val trimmed = text.trim()
    return trimmed.isNotEmpty() && trimmed.length <= 4000
}

/** The chat with one more message, and my unread count back to zero (I'm looking at it). */
fun withMessage(chat: Chat, message: Message): Chat = chat.copy(messages = chat.messages + message, unread = 0)

/** Moves my message one step on: sending → sent → delivered → read. */
fun advanceStatus(status: MessageStatus): MessageStatus = when (status) {
    MessageStatus.Sending -> MessageStatus.Sent
    MessageStatus.Sent -> MessageStatus.Delivered
    else -> MessageStatus.Read
}

data class DayGroup(val day: String, val messages: List<Message>)

/** Messages grouped under day headers ("Today", "Yesterday", ...), oldest day first. */
fun groupByDay(messages: List<Message>, now: Long, zone: TimeZone = TimeZone.getDefault()): List<DayGroup> {
    val groups = mutableListOf<DayGroup>()
    for (message in messages) {
        val label = dayLabel(message.sentAt, now, zone)
        val current = groups.lastOrNull()
        if (current != null && current.day == label) groups[groups.size - 1] = current.copy(messages = current.messages + message)
        else groups.add(DayGroup(label, listOf(message)))
    }
    return groups
}

fun dayLabel(at: Long, now: Long, zone: TimeZone = TimeZone.getDefault()): String {
    val label = formatTime(at, now, zone)
    return if (label.contains(':')) "Today" else label
}

private val WHITESPACE = Regex("[\\s\\p{Z}\\uFEFF]+")

/** Initials for an avatar: "Asha Rao" → "AR", "Weekend trip" → "WT". */
fun initials(name: String): String =
    name.split(WHITESPACE).filter { it.isNotEmpty() }.take(2).joinToString("") { it[0].uppercase() }.ifEmpty { "?" }
