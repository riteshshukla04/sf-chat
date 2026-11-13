package dev.softwarefactory.chatter.ui.screens

import dev.softwarefactory.chatter.core.Chat
import dev.softwarefactory.chatter.core.Contact
import dev.softwarefactory.chatter.core.MessageStatus
import dev.softwarefactory.chatter.core.formatTime
import dev.softwarefactory.chatter.core.lastActivityAt
import dev.softwarefactory.chatter.core.previewText
import dev.softwarefactory.chatter.core.searchChats
import dev.softwarefactory.chatter.core.sortChats
import java.text.Collator
import java.util.Locale
import java.util.TimeZone

/** Pure view-state for the Chats list (port of the row logic in src/app/(tabs)/index.tsx), unit-testable on the JVM. */
data class ChatRowModel(
    val id: String,
    val title: String,
    val group: Boolean,
    val time: String,
    /** "✓✓ ", "🕓 " or "✓ " before my last message, else "". */
    val tickPrefix: String,
    val preview: String,
    val pinned: Boolean,
    val muted: Boolean,
    val unread: Int,
) {
    /** Time is accent-coloured only when there are unread messages and the chat isn't muted. */
    val timeAccent: Boolean get() = unread > 0 && !muted
    /** Badge is grey when muted, accent otherwise. */
    val badgeMuted: Boolean get() = muted
    val subtitle: String get() = tickPrefix + preview
}

fun tickPrefix(chat: Chat): String {
    val last = chat.messages.lastOrNull() ?: return ""
    if (last.from != "me") return ""
    return when (last.status) {
        MessageStatus.Read -> "✓✓ "
        MessageStatus.Sending -> "🕓 "
        else -> "✓ "
    }
}

fun chatRowModel(chat: Chat, contacts: Map<String, Contact>, now: Long, zone: TimeZone = TimeZone.getDefault()) = ChatRowModel(
    id = chat.id,
    title = chat.title,
    group = chat.kind == dev.softwarefactory.chatter.core.ChatKind.Group,
    time = formatTime(lastActivityAt(chat), now, zone),
    tickPrefix = tickPrefix(chat),
    preview = previewText(chat, contacts),
    pinned = chat.pinned,
    muted = chat.muted,
    unread = chat.unread,
)

/** Filtered by the query, then pinned first / most recent first. */
fun visibleChats(chats: List<Chat>, query: String): List<Chat> = sortChats(searchChats(chats, query))

/** The text of the empty state; uses the raw (untrimmed) query like the RN app. */
fun emptyChatsMessage(query: String) = "No chats match “$query”"

/** New chat: contacts sorted by name (locale-aware, like JS localeCompare). */
fun sortedContacts(contacts: Map<String, Contact>, locale: Locale = Locale.getDefault()): List<Contact> {
    val collator = Collator.getInstance(locale)
    return contacts.values.sortedWith { a, b -> collator.compare(a.name, b.name) }
}
