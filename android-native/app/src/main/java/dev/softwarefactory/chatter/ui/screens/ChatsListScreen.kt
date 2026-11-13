package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import dev.softwarefactory.chatter.domain.Chat
import dev.softwarefactory.chatter.domain.previewText
import dev.softwarefactory.chatter.domain.sortChats
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.TestTags
import dev.softwarefactory.chatter.ui.theme.Spacing

/** PLACEHOLDER: the chats-list task replaces the body of this file (keep the signature or update NavRoot). */
@Composable
fun ChatsListScreen(state: AppState, onOpenChat: (Chat) -> Unit, onNewChat: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(Spacing.lg)) {
        Text("Chats (placeholder)")
        Button(onClick = onNewChat, modifier = Modifier.testTag(TestTags.NewChat)) { Text("New chat") }
        LazyColumn {
            items(sortChats(state.chats), key = { it.id }) { chat ->
                Column(
                    Modifier.fillMaxWidth().testTag(TestTags.chatRow(chat.id)).clickable { onOpenChat(chat) }.padding(vertical = Spacing.sm),
                ) {
                    Text(chat.title)
                    Text(previewText(chat, state.contacts))
                }
            }
        }
    }
}
