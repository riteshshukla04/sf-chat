package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import dev.softwarefactory.chatter.ui.ChatHeader
import dev.softwarefactory.chatter.ui.nav.ProvideHeader
import dev.softwarefactory.chatter.ui.theme.ChatterTheme

/**
 * STUB: replaced by the chat screen task. Keep the signature so the nav graph does not change.
 * The shell draws the top bar (back arrow); the screen fills its title via [ProvideHeader].
 */
@Composable
fun ChatScreen(chatId: String, modifier: Modifier = Modifier) {
    val c = ChatterTheme.colors
    ProvideHeader { ChatHeader(title = "Chat $chatId", subtitle = "", group = false) }
    Box(modifier.fillMaxSize().background(c.chatBackground).testTag("chat-screen"), contentAlignment = Alignment.Center) {
        Text("Chat $chatId", color = c.muted)
    }
}
