package dev.softwarefactory.chatter.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.softwarefactory.chatter.AppContainer
import dev.softwarefactory.chatter.core.totalUnread

/** The badge on the Chats tab: unread messages over all chats that aren't muted. */
@Composable
fun rememberTotalUnread(): Int {
    val state by AppContainer.store.state.collectAsState()
    return totalUnread(state.chats)
}
