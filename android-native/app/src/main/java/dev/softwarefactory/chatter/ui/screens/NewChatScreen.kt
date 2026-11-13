package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.components.Avatar
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import dev.softwarefactory.chatter.ui.theme.Spacing

object NewChatTags {
    const val Screen = "new-chat-screen"
    fun contact(id: String) = "contact-$id"
}

/** New-chat modal: contacts sorted by name; tapping one calls [onStartChat] (NavRoot starts the chat and opens it). */
@Composable
fun NewChatScreen(state: AppState, onStartChat: (contactId: String) -> Unit, onClose: () -> Unit) {
    val c = ChatterTheme.colors
    val contacts = remember(state.contacts) { sortedContacts(state.contacts) }
    Column(Modifier.fillMaxSize().background(c.surface).testTag(NewChatTags.Screen)) {
    // Modal header with the title RN gives the route ("New chat") and a way to close it.
    Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.xs, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClose, modifier = Modifier.testTag("new-chat-close")) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = c.accent)
        }
        Text("New chat", color = c.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = Spacing.sm))
    }
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Text(
                "Contacts on Chatter", color = c.muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.lg, bottom = Spacing.sm),
            )
        }
        items(contacts, key = { it.id }) { contact ->
            Row(
                Modifier.fillMaxWidth().testTag(NewChatTags.contact(contact.id)).clickable { onStartChat(contact.id) }
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Avatar(contact.name, size = 44.dp)
                Column(Modifier.weight(1f)) {
                    Text(contact.name, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(contact.about, color = c.muted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
}
