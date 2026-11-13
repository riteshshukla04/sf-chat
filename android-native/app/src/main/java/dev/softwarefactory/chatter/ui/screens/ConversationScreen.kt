package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.domain.Chat
import dev.softwarefactory.chatter.domain.ChatKind
import dev.softwarefactory.chatter.domain.canSend
import dev.softwarefactory.chatter.domain.headerSubtitle
import dev.softwarefactory.chatter.domain.initials
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.TestTags
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import dev.softwarefactory.chatter.ui.theme.Spacing

const val MissingChatText = "This chat doesn't exist any more."

/**
 * Conversation (src/app/chat/[id].tsx). Route: chat/<id>.
 * [onMarkRead] runs when the chat is opened; [onSend] hands a draft to the store, which advances the ticks and simulates the reply.
 */
@Composable
fun ConversationScreen(
    chatId: String,
    state: AppState,
    now: Long,
    onBack: () -> Unit,
    onMarkRead: (String) -> Unit = {},
    onSend: (chatId: String, text: String) -> Unit = { _, _ -> },
) {
    val c = ChatterTheme.colors
    val chat = state.chats.firstOrNull { it.id == chatId }

    LaunchedEffect(chatId) { onMarkRead(chatId) }

    if (chat == null) {
        Box(Modifier.fillMaxSize().background(c.background), contentAlignment = Alignment.Center) {
            Text(MissingChatText, color = c.muted)
        }
        return
    }

    var draft by rememberSaveable(chatId) { mutableStateOf("") }
    val rows = remember(chat, state.contacts, now) { conversationRows(chat, state.contacts, now) }
    val sendable = canSend(draft)

    Column(Modifier.fillMaxSize().background(c.chatBackground).imePadding().testTag("chat-screen")) {
        Header(chat, headerSubtitle(chat, state.contacts, now), onBack)
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true, // newest at the bottom
            contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(rows.asReversed(), key = { it.key }) { row ->
                when (row) {
                    is ConversationRow.Day -> DayHeader(row.day)
                    is ConversationRow.Bubble -> Bubble(row)
                }
            }
            if (chat.messages.isEmpty()) {
                item(key = "empty-notice") {
                    Box(
                        Modifier.fillMaxWidth().padding(Spacing.lg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Messages are kept on this phone. Say hi 👋",
                            color = c.muted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(c.surfaceAlt).padding(Spacing.md),
                        )
                    }
                }
            }
        }
        InputBar(
            draft = draft,
            onDraft = { draft = it },
            sendable = sendable,
            onSend = {
                if (canSend(draft)) {
                    onSend(chat.id, draft)
                    draft = ""
                }
            },
        )
    }
}

@Composable
private fun Header(chat: Chat, subtitle: String, onBack: () -> Unit) {
    val c = ChatterTheme.colors
    Row(
        Modifier.fillMaxWidth().background(c.surface).padding(horizontal = Spacing.xs, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("back")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = c.text)
        }
        Avatar(chat.title, 36.dp, chat.kind == ChatKind.Group)
        Column(Modifier.padding(start = Spacing.sm).weight(1f)) {
            Text(chat.title, color = c.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = c.muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.testTag(TestTags.ChatSubtitle))
        }
    }
}

@Composable
fun Avatar(name: String, size: androidx.compose.ui.unit.Dp, group: Boolean = false) {
    Box(
        Modifier.size(size).clip(CircleShape).background(Color(0xFF000000 or avatarColour(name))),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials(name), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * if (group) 0.34f else 0.38f).sp)
    }
}

@Composable
private fun DayHeader(day: String) {
    val c = ChatterTheme.colors
    Box(Modifier.fillMaxWidth().padding(vertical = Spacing.sm), contentAlignment = Alignment.Center) {
        Text(
            day, color = c.muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(c.surfaceAlt).padding(horizontal = Spacing.md, vertical = 4.dp),
        )
    }
}

@Composable
private fun Bubble(row: ConversationRow.Bubble) {
    val c = ChatterTheme.colors
    val m = row.message
    val shape = RoundedCornerShape(
        topStart = if (row.mine) 12.dp else 2.dp, topEnd = if (row.mine) 2.dp else 12.dp, bottomStart = 12.dp, bottomEnd = 12.dp,
    )
    Box(Modifier.fillMaxWidth(), contentAlignment = if (row.mine) Alignment.CenterEnd else Alignment.CenterStart) {
        Column(
            Modifier
                .widthIn(max = 320.dp)
                .testTag("message-${m.id}")
                .clip(shape)
                .background(if (row.mine) c.bubbleMine else c.bubbleTheirs)
                .padding(start = Spacing.md, end = Spacing.md, top = 6.dp, bottom = 4.dp),
        ) {
            if (row.sender != null) {
                Text(row.sender, color = c.accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
            }
            Text(m.text, color = c.text, fontSize = 16.sp, lineHeight = 21.sp)
            Row(Modifier.align(Alignment.End).padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(row.clock, color = c.muted, fontSize = 11.sp)
                if (row.mine) {
                    Icon(
                        when (tickFor(m.status)) {
                            Tick.Clock -> Icons.Outlined.Schedule
                            Tick.Single -> Icons.Filled.Done
                            Tick.Double -> Icons.Filled.DoneAll
                        },
                        contentDescription = m.status.name.lowercase(),
                        tint = if (tickIsRead(m.status)) c.tickRead else c.muted,
                        modifier = Modifier.size(15.dp).testTag(tickTag(m.status)),
                    )
                }
            }
        }
    }
}

@Composable
private fun InputBar(draft: String, onDraft: (String) -> Unit, sendable: Boolean, onSend: () -> Unit) {
    val c = ChatterTheme.colors
    Row(Modifier.fillMaxWidth().padding(Spacing.sm), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Box(
            Modifier.weight(1f).heightIn(min = 46.dp, max = 120.dp).clip(RoundedCornerShape(22.dp)).background(c.surface)
                .padding(horizontal = Spacing.lg, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (draft.isEmpty()) Text("Message", color = c.muted, fontSize = 16.sp)
            BasicTextField(
                value = draft,
                onValueChange = onDraft,
                textStyle = TextStyle(color = c.text, fontSize = 16.sp),
                cursorBrush = SolidColor(c.accent),
                modifier = Modifier.fillMaxWidth().testTag(TestTags.MessageInput),
            )
        }
        IconButton(
            onClick = onSend,
            enabled = sendable,
            modifier = Modifier.size(46.dp).clip(CircleShape).background(c.accent.copy(alpha = if (sendable) 1f else 0.5f)).testTag(TestTags.Send),
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = c.onAccent, modifier = Modifier.size(22.dp))
        }
    }
}
