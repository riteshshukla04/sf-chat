package dev.softwarefactory.chatter.core

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Calendar
import java.util.TimeZone

/**
 * Chat logic: a port of src/lib/chats.ts. Pure Kotlin (no Android imports), so it unit-tests on the JVM.
 * Time functions take an injectable [TimeZone]; "now" is always passed in.
 * The @Serializable annotations give exactly the JSON shape of the React Native app (key 'chatter:v1').
 */

@Serializable
enum class MessageStatus {
    @SerialName("sending") Sending,
    @SerialName("sent") Sent,
    @SerialName("delivered") Delivered,
    @SerialName("read") Read;

    /** The lower-case name used in the stored JSON and in test ids (`ticks-<status>`). */
    val wire: String get() = name.lowercase()
}

@Serializable
enum class ChatKind {
    @SerialName("direct") Direct,
    @SerialName("group") Group,
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
    /** emoji → ids of the people ("me" or contact ids). Absent (null) on old messages. */
    val reactions: Map<String, List<String>>? = null,
)

val REACTION_EMOJIS: List<String> = listOf("👍", "❤️", "😂", "😮", "😢", "🙏")

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Contact(
    val id: String,
    val name: String,
    @EncodeDefault val about: String = "",
    /** Epoch milliseconds the contact was last online, or null when online now. Always written, even as null. */
    @EncodeDefault val lastSeenAt: Long? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Chat(
    val id: String,
    val kind: ChatKind,
    val title: String,
    /** Contact ids (without "me"). */
    @EncodeDefault val members: List<String> = emptyList(),
    /** Oldest first. */
    @EncodeDefault val messages: List<Message> = emptyList(),
    /** How many of the newest messages I haven't read. */
    @EncodeDefault val unread: Int = 0,
    @EncodeDefault val pinned: Boolean = false,
    @EncodeDefault val muted: Boolean = false,
    @EncodeDefault val createdAt: Long = 0,
)

private const val MINUTE = 60_000L
private const val DAY = 24 * 60 * MINUTE
private val WEEKDAYS = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

private fun pad(n: Int) = n.toString().padStart(2, '0')

private fun calendar(at: Long, zone: TimeZone): Calendar =
    Calendar.getInstance(zone).apply { timeInMillis = at }

/** "14:05" today, "Yesterday", the weekday within a week, else "12/11/2026". */
fun formatTime(at: Long, now: Long, zone: TimeZone = TimeZone.getDefault()): String {
    val time = calendar(at, zone)
    val today = calendar(now, zone)
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
    val time = calendar(at, zone)
    return "${pad(time.get(Calendar.HOUR_OF_DAY))}:${pad(time.get(Calendar.MINUTE))}"
}

/** When something last happened in a chat: its newest message, or when it was created. */
fun lastActivityAt(chat: Chat): Long = chat.messages.lastOrNull()?.sentAt ?: chat.createdAt

/** Pinned chats first, then the most recently active. Stable, like JS Array.sort. */
fun sortChats(chats: List<Chat>): List<Chat> =
    chats.sortedWith { a, b ->
        val pin = (if (b.pinned) 1 else 0) - (if (a.pinned) 1 else 0)
        if (pin != 0) pin else lastActivityAt(b).compareTo(lastActivityAt(a))
    }

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

/** The line under the name in a conversation's header. */
fun headerSubtitle(chat: Chat, contacts: Map<String, Contact>, now: Long, zone: TimeZone = TimeZone.getDefault()): String {
    if (chat.kind == ChatKind.Group) {
        val names = chat.members.map { firstName(contacts[it]?.name ?: "Someone") }
        val last = chat.messages.lastOrNull()
        val members = "${names.joinToString(", ")}, You"
        return if (last != null) "$members · last message ${formatTime(last.sentAt, now, zone)}" else members
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

/** Initials for an avatar: "Asha Rao" → "AR", "Weekend trip" → "WT". */
fun initials(name: String): String {
    val letters = name.split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2)
        .joinToString("") { firstCodePoint(it).uppercase() }
    return letters.ifEmpty { "?" }
}

// JS `part[0]` is a UTF-16 unit; use the whole first code point so emoji aren't split into lone surrogates.
private fun firstCodePoint(s: String): String = String(Character.toChars(s.codePointAt(0)))

/**
 * Toggles a person's reaction on a message. Reacting with the emoji they already used removes it;
 * a different emoji replaces their earlier one (one reaction per person). Unknown emojis are ignored.
 */
fun toggleReaction(message: Message, emoji: String, by: String = "me"): Message {
    if (emoji !in REACTION_EMOJIS) return message
    val next = LinkedHashMap<String, List<String>>()
    val had = message.reactions?.get(emoji)?.contains(by) ?: false
    for ((e, people) in message.reactions ?: emptyMap()) {
        val rest = people.filter { it != by }
        if (rest.isNotEmpty()) next[e] = rest
    }
    if (!had) next[emoji] = (next[emoji] ?: emptyList()) + by
    return message.copy(reactions = next.ifEmpty { null })
}

data class ReactionPill(val emoji: String, val count: Int, val mine: Boolean)

/** Reactions as pills, in the picker's order: emoji, count, and whether I used it. */
fun reactionSummary(message: Message): List<ReactionPill> =
    REACTION_EMOJIS.mapNotNull { emoji ->
        val people = message.reactions?.get(emoji)
        if (!people.isNullOrEmpty()) ReactionPill(emoji, people.size, people.contains("me")) else null
    }
