package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.domain.Chat
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.TestTags
import dev.softwarefactory.chatter.ui.components.Avatar
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import dev.softwarefactory.chatter.ui.theme.Spacing

/** Extra RN testIDs of this screen (see src/app/(tabs)/index.tsx). */
object ChatsTags {
    const val Screen = "chats-screen"
    const val Search = "chat-search"
}

/**
 * Chats tab: search, pinned-first list, rows open the chat, long press toggles pin, FAB starts a new chat.
 * Port of src/app/(tabs)/index.tsx. [releaseNote] is the optional EXPO_PUBLIC_RELEASE_NOTE (OTA, null natively).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatsListScreen(
    state: AppState,
    onOpenChat: (Chat) -> Unit,
    onNewChat: () -> Unit,
    onTogglePin: (chatId: String) -> Unit = {},
    now: Long = System.currentTimeMillis(),
    releaseNote: String? = null,
) {
    val c = ChatterTheme.colors
    var query by rememberSaveable { mutableStateOf("") }
    val shown = remember(state.chats, query) { visibleChats(state.chats, query) }

    Box(Modifier.fillMaxSize().background(c.surface).testTag(ChatsTags.Screen)) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.sm, bottom = Spacing.sm)) {
                Text("Chatter", color = c.accent, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                if (!releaseNote.isNullOrEmpty()) {
                    Text(releaseNote, Modifier.padding(top = 2.dp).testTag("release-note"), color = c.muted, fontSize = 13.sp)
                }
            }
            Row(
                Modifier.padding(horizontal = Spacing.lg).padding(bottom = Spacing.sm).fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp)).background(c.surfaceAlt).padding(horizontal = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = c.muted, modifier = Modifier.size(20.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = c.text, fontSize = 16.sp),
                    cursorBrush = SolidColor(c.text),
                    modifier = Modifier.weight(1f).testTag(ChatsTags.Search),
                    decorationBox = { inner ->
                        Box(Modifier.padding(vertical = 10.dp)) {
                            if (query.isEmpty()) Text("Search", color = c.muted, fontSize = 16.sp)
                            inner()
                        }
                    },
                )
            }
            LazyColumn(Modifier.weight(1f)) {
                if (shown.isEmpty()) {
                    item {
                        Text(
                            emptyMessage(query),
                            Modifier.fillMaxWidth().padding(top = Spacing.xl),
                            color = c.muted,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                items(shown, key = { it.id }) { chat ->
                    ChatRow(
                        chatRowModel(chat, state.contacts, now),
                        onClick = { onOpenChat(chat) },
                        onLongClick = { onTogglePin(chat.id) },
                    )
                }
            }
        }
        Box(
            Modifier.align(Alignment.BottomEnd).padding(Spacing.lg).size(58.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(c.accent)
                .combinedClickable(onClick = onNewChat).testTag(TestTags.NewChat),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.AddComment, contentDescription = "New chat", tint = c.onAccent, modifier = Modifier.size(26.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ChatRow(m: ChatRowModel, onClick: () -> Unit, onLongClick: () -> Unit) {
    val c = ChatterTheme.colors
    Row(
        Modifier.fillMaxWidth().testTag(TestTags.chatRow(m.id)).combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Avatar(m.title, group = m.group)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(m.title, Modifier.weight(1f), color = c.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(m.time, color = if (m.timeHighlighted) c.accent else c.muted, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(m.statusPrefix + m.preview, Modifier.weight(1f), color = c.muted, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (m.muted) Icon(Icons.Outlined.NotificationsOff, "Muted", Modifier.size(16.dp), tint = c.muted)
                    if (m.pinned) Icon(Icons.Outlined.PushPin, "Pinned", Modifier.size(16.dp), tint = c.muted)
                    if (m.unread > 0) {
                        Box(
                            Modifier.widthIn(min = 22.dp).height(22.dp).clip(CircleShape)
                                .background(if (m.muted) c.muted else c.accent).padding(horizontal = 6.dp)
                                .semantics { contentDescription = "${m.unread} unread" },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(m.unread.toString(), color = c.onAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
