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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.AppContainer
import dev.softwarefactory.chatter.core.Contact
import dev.softwarefactory.chatter.ui.Avatar
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import dev.softwarefactory.chatter.ui.theme.Spacing

/** Port of src/app/new-chat.tsx. [onClose] is unused: the shell's back button handles dismissal. */
@Composable
fun NewChatScreen(onClose: () -> Unit, onOpenChat: (chatId: String) -> Unit, modifier: Modifier = Modifier) {
    val state by AppContainer.store.state.collectAsState()
    NewChatContent(
        contacts = state.contacts,
        // The shell replaces new-chat with the chat, so back goes to the list.
        onPick = { contactId -> onOpenChat(AppContainer.store.startChat(contactId)) },
        modifier = modifier,
    )
}

@Composable
fun NewChatContent(contacts: Map<String, Contact>, onPick: (contactId: String) -> Unit, modifier: Modifier = Modifier) {
    val c = ChatterTheme.colors
    val sorted = remember(contacts) { sortedContacts(contacts) }
    LazyColumn(modifier.fillMaxSize().background(c.surface).testTag("new-chat-screen")) {
        item("header") {
            Text(
                "Contacts on Chatter", color = c.muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.lg, bottom = Spacing.sm),
            )
        }
        items(sorted, key = { it.id }) { contact ->
            Row(
                Modifier.fillMaxWidth().clickable { onPick(contact.id) }
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md).testTag("contact-${contact.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Avatar(name = contact.name, size = 44.dp)
                Column(Modifier.weight(1f)) {
                    Text(contact.name, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(contact.about, color = c.muted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
