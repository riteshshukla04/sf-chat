package dev.softwarefactory.chatter.ui

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.softwarefactory.chatter.domain.totalUnread
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.screens.CallsScreen
import dev.softwarefactory.chatter.ui.screens.ChatsListScreen
import dev.softwarefactory.chatter.ui.screens.ConversationScreen
import dev.softwarefactory.chatter.ui.screens.NewChatScreen
import dev.softwarefactory.chatter.ui.screens.SettingsScreen
import dev.softwarefactory.chatter.ui.theme.ChatterTheme

/** Bottom tabs: titles and icons as in src/app/(tabs)/_layout.tsx (message-text, phone, cog outline). */
enum class Tab(val title: String, val icon: ImageVector, val tag: String) {
    Chats("Chats", Icons.AutoMirrored.Outlined.Chat, TestTags.TabChats),
    Calls("Calls", Icons.Outlined.Call, TestTags.TabCalls),
    Settings("Settings", Icons.Outlined.Settings, TestTags.TabSettings),
}

/** Everything the shell needs from the store, so the UI can be previewed and tested without one. */
class NavActions(
    val startChat: (contactId: String) -> String,
    val markRead: (chatId: String) -> Unit,
    val now: () -> Long,
    val sendMessage: (chatId: String, text: String) -> Unit = { _, _ -> },
    val togglePin: (chatId: String) -> Unit = {},
)

/** Root: hydration gate (nothing until [AppState.hydrated], an error screen if it failed), then the nav graph. */
@Composable
fun AppRoot(state: AppState, hydrationError: String?, onRetry: () -> Unit, actions: NavActions) {
    ChatterTheme {
        Box(Modifier.fillMaxSize()) {
            when {
                hydrationError != null -> ErrorScreen(hydrationError, onRetry)
                state.hydrated -> NavRoot(state, actions)
                else -> Box(Modifier.fillMaxSize()) // like RN: render nothing while loading
            }
        }
    }
}

@Composable
fun NavRoot(state: AppState, actions: NavActions) {
    val nav = rememberNavController()
    NavHost(nav, startDestination = Routes.Tabs) {
        composable(Routes.Tabs) {
            TabsScaffold(
                state,
                onOpenChat = { nav.navigate(Routes.chat(it)) },
                onNewChat = { nav.navigate(Routes.NewChat) },
                onTogglePin = actions.togglePin,
                now = actions.now(),
            )
        }
        composable(Routes.Chat, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            Box(Modifier.safeDrawingPadding()) { ConversationScreen(
                    id, state, actions.now(), onBack = { nav.popBackStack() },
                    onMarkRead = actions.markRead, onSend = actions.sendMessage,
                ) }
        }
        composable(
            Routes.NewChat,
            enterTransition = { slideInVertically { it } },
            popExitTransition = { slideOutVertically { it } },
        ) {
            Box(Modifier.safeDrawingPadding()) {
                NewChatScreen(
                    state,
                    onStartChat = { contactId ->
                        val id = actions.startChat(contactId)
                        nav.popBackStack()
                        nav.navigate(Routes.chat(id))
                    },
                    onClose = { nav.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun TabsScaffold(state: AppState, onOpenChat: (String) -> Unit, onNewChat: () -> Unit, onTogglePin: (String) -> Unit, now: Long) {
    var selected by rememberSaveable { mutableStateOf(Tab.Chats) }
    val unread = totalUnread(state.chats)
    val c = ChatterTheme.colors
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = c.surface) {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selected == tab,
                        onClick = { selected = tab },
                        modifier = Modifier.testTag(tab.tag),
                        icon = {
                            if (tab == Tab.Chats && unread > 0) {
                                BadgedBox(badge = { Badge { Text(unread.toString()) } }) { Icon(tab.icon, contentDescription = null) }
                            } else {
                                Icon(tab.icon, contentDescription = null)
                            }
                        },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = c.accent,
                            selectedTextColor = c.accent,
                            unselectedIconColor = c.muted,
                            unselectedTextColor = c.muted,
                            indicatorColor = c.surfaceAlt,
                        ),
                    )
                }
            }
        },
        containerColor = c.background,
    ) { padding ->
        Box(Modifier.padding(padding).statusBarsPadding()) {
            when (selected) {
                Tab.Chats -> ChatsListScreen(state, onOpenChat = { onOpenChat(it.id) }, onNewChat = onNewChat, onTogglePin = onTogglePin, now = now)
                Tab.Calls -> CallsScreen(state)
                Tab.Settings -> SettingsScreen(state)
            }
        }
    }
}
