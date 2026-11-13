package dev.softwarefactory.chatter.ui.screens

import dev.softwarefactory.chatter.domain.Chat
import dev.softwarefactory.chatter.domain.ChatKind
import dev.softwarefactory.chatter.domain.Contact
import dev.softwarefactory.chatter.domain.MessageStatus
import dev.softwarefactory.chatter.domain.formatTime
import dev.softwarefactory.chatter.domain.lastActivityAt
import dev.softwarefactory.chatter.domain.previewText
import dev.softwarefactory.chatter.domain.searchChats
import dev.softwarefactory.chatter.domain.sortChats
import java.text.Collator
import java.util.TimeZone

/** Everything a chat row shows, computed from the domain (pure, unit-tested). */
data class ChatRowModel(
    val id: String,
    val title: String,
    val group: Boolean,
    val time: String,
    /** Time is accent coloured when unread and not muted. */
    val timeHighlighted: Boolean,
    /** "✓✓ ", "🕓 ", "✓ " for my own last message, else "". */
    val statusPrefix: String,
    val preview: String,
    val muted: Boolean,
    val pinned: Boolean,
    val unread: Int,
)

fun chatRowModel(chat: Chat, contacts: Map<String, Contact>, now: Long, zone: TimeZone = TimeZone.getDefault()): ChatRowModel {
    val last = chat.messages.lastOrNull()
    val prefix = when {
        last == null || last.from != "me" -> ""
        last.status == MessageStatus.Read -> "✓✓ "
        last.status == MessageStatus.Sending -> "🕓 "
        else -> "✓ "
    }
    return ChatRowModel(
        id = chat.id,
        title = chat.title,
        group = chat.kind == ChatKind.Group,
        time = formatTime(lastActivityAt(chat), now, zone),
        timeHighlighted = chat.unread > 0 && !chat.muted,
        statusPrefix = prefix,
        preview = previewText(chat, contacts),
        muted = chat.muted,
        pinned = chat.pinned,
        unread = chat.unread,
    )
}

/** searchChats then sortChats, as the RN screen does. */
fun visibleChats(chats: List<Chat>, query: String): List<Chat> = sortChats(searchChats(chats, query))

fun emptyMessage(query: String) = "No chats match “$query”"

/** Contacts of the new-chat modal, sorted by name (localeCompare). */
fun sortedContacts(contacts: Map<String, Contact>): List<Contact> {
    val collator = Collator.getInstance()
    return contacts.values.sortedWith { a, b -> collator.compare(a.name, b.name) }
}
