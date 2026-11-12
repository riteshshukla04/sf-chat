package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import dev.softwarefactory.chatter.ui.theme.ChatterTheme


/** STUB: replaced by the NewChatScreen task. Keep the signature so the nav graph does not change. */
@Composable
fun NewChatScreen(onClose: () -> Unit, onOpenChat: (chatId: String) -> Unit, modifier: Modifier = Modifier) {
    val c = ChatterTheme.colors
    Box(modifier.fillMaxSize().background(c.background).testTag("new-chat-screen"), contentAlignment = Alignment.Center) {
        Text("New chat", color = c.muted)
    }
}
