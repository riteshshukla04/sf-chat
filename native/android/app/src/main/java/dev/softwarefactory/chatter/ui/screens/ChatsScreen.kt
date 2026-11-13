package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.AppContainer
import dev.softwarefactory.chatter.BuildConfig
import dev.softwarefactory.chatter.core.Chat
import dev.softwarefactory.chatter.core.Contact
import dev.softwarefactory.chatter.ui.Avatar
import dev.softwarefactory.chatter.ui.rememberNow
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import dev.softwarefactory.chatter.ui.theme.Spacing

/** Port of src/app/(tabs)/index.tsx. */
@Composable
fun ChatsScreen(onOpenChat: (chatId: String) -> Unit, onNewChat: () -> Unit, modifier: Modifier = Modifier) {
    val state by AppContainer.store.state.collectAsState()
    ChatsContent(
        chats = state.chats,
        contacts = state.contacts,
        onOpenChat = onOpenChat,
        onNewChat = onNewChat,
        onTogglePin = { AppContainer.store.togglePin(it) },
        modifier = modifier,
        releaseNote = BuildConfig.RELEASE_NOTE,
    )
}

/** Stateless (apart from the search text) so it can be tested with plain data. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatsContent(
    chats: List<Chat>,
    contacts: Map<String, Contact>,
    onOpenChat: (String) -> Unit,
    onNewChat: () -> Unit,
    onTogglePin: (String) -> Unit,
    modifier: Modifier = Modifier,
    releaseNote: String = "",
    now: Long = rememberNow(),
) {
    val c = ChatterTheme.colors
    var query by rememberSaveable { mutableStateOf("") }
    val shown = remember(chats, query) { visibleChats(chats, query) }

    Box(modifier.fillMaxSize().background(c.surface).testTag("chats-screen")) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.sm, bottom = Spacing.sm)) {
                Text("Chatter", color = c.accent, fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
                if (releaseNote.isNotEmpty()) {
                    Text(releaseNote, color = c.muted, fontSize = 13.sp, modifier = Modifier.padding(top = 2.dp).testTag("release-note"))
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
                    cursorBrush = SolidColor(c.accent),
                    modifier = Modifier.weight(1f).padding(vertical = 12.dp).testTag("chat-search"),
                    decorationBox = { inner ->
                        Box { if (query.isEmpty()) Text("Search", color = c.muted, fontSize = 16.sp); inner() }
                    },
                )
            }
            LazyColumn(Modifier.fillMaxSize()) {
                if (shown.isEmpty()) {
                    item("empty") {
                        Text(
                            emptyChatsMessage(query), color = c.muted, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl),
                        )
                    }
                }
                items(shown, key = { it.id }) { chat ->
                    ChatRow(
                        model = chatRowModel(chat, contacts, now),
                        onClick = { onOpenChat(chat.id) },
                        onLongClick = { onTogglePin(chat.id) },
                    )
                }
            }
        }
        Box(
            Modifier.align(Alignment.BottomEnd).padding(Spacing.lg).size(58.dp)
                .shadow(4.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(c.accent)
                .combinedClickable(onClick = onNewChat).testTag("new-chat"),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.AddComment, contentDescription = "New chat", tint = c.onAccent, modifier = Modifier.size(26.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatRow(model: ChatRowModel, onClick: () -> Unit, onLongClick: () -> Unit) {
    val c = ChatterTheme.colors
    Row(
        Modifier.fillMaxWidth().background(c.surface)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md).testTag("chat-row-${model.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Avatar(name = model.title, group = model.group)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(model.title, color = c.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(model.time, color = if (model.timeAccent) c.accent else c.muted, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(model.subtitle, color = c.muted, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (model.muted) Icon(Icons.Outlined.NotificationsOff, "Muted", tint = c.muted, modifier = Modifier.size(16.dp))
                    if (model.pinned) Icon(Icons.Outlined.PushPin, "Pinned", tint = c.muted, modifier = Modifier.size(16.dp))
                    if (model.unread > 0) {
                        Box(
                            Modifier.defaultMinSize(minWidth = 22.dp).height(22.dp).clip(CircleShape)
                                .background(if (model.badgeMuted) c.muted else c.accent).padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(model.unread.toString(), color = c.onAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
