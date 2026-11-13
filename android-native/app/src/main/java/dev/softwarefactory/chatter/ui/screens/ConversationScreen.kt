package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import dev.softwarefactory.chatter.domain.headerSubtitle
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.TestTags
import dev.softwarefactory.chatter.ui.theme.Spacing

/** PLACEHOLDER: the conversation task replaces this file. Route: chat/<id>. */
@Composable
fun ConversationScreen(chatId: String, state: AppState, now: Long, onBack: () -> Unit) {
    val chat = state.chats.firstOrNull { it.id == chatId }
    Column(Modifier.fillMaxSize().padding(Spacing.lg)) {
        Text("Conversation (placeholder): ${chat?.title ?: "unknown chat"}")
        if (chat != null) {
            Text(headerSubtitle(chat, state.contacts, now), modifier = Modifier.testTag(TestTags.ChatSubtitle))
        }
    }
}
