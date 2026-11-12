package dev.softwarefactory.chatter.ui.nav

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.softwarefactory.chatter.ui.screens.CallsScreen
import dev.softwarefactory.chatter.ui.screens.ChatScreen
import dev.softwarefactory.chatter.ui.screens.ChatsScreen
import dev.softwarefactory.chatter.ui.screens.NewChatScreen
import dev.softwarefactory.chatter.ui.screens.SettingsScreen
import dev.softwarefactory.chatter.ui.theme.ChatterTheme

object Routes {
    const val CHATS = "chats"
    const val CALLS = "calls"
    const val SETTINGS = "settings"
    const val CHAT = "chat/{id}"
    const val NEW_CHAT = "new-chat"
    fun chat(id: String) = "chat/${android.net.Uri.encode(id)}"
}

private data class Tab(val route: String, val label: String, val tag: String, val icon: ImageVector)

private val Tabs = listOf(
    Tab(Routes.CHATS, "Chats", "tab-chats", Icons.AutoMirrored.Outlined.Message),
    Tab(Routes.CALLS, "Calls", "tab-calls", Icons.Outlined.Phone),
    Tab(Routes.SETTINGS, "Settings", "tab-settings", Icons.Outlined.Settings),
)

/** Navigation shell: bottom tabs (Chats / Calls / Settings), chat/{id} and a modal new-chat route. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatterApp(modifier: Modifier = Modifier, navController: NavHostController = rememberNavController()) {
    val c = ChatterTheme.colors
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val isTab = route == null || Tabs.any { it.route == route }
    val isChat = route == Routes.CHAT
    val isNewChat = route == Routes.NEW_CHAT
    val headerSlot = remember { HeaderSlot() }
    val unread = rememberTotalUnread()

    androidx.compose.runtime.CompositionLocalProvider(LocalHeaderSlot provides headerSlot) {
        Scaffold(
            modifier = modifier,
            containerColor = c.background,
            topBar = {
                if (isChat || isNewChat) {
                    TopAppBar(
                        title = { if (isNewChat) Text("New chat", color = c.text) else headerSlot.content?.invoke() },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.testTag("back")) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = c.accent)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = c.surface, titleContentColor = c.text),
                    )
                }
            },
            bottomBar = {
                if (isTab) {
                    NavigationBar(containerColor = c.surface) {
                        for (tab in Tabs) {
                            NavigationBarItem(
                                selected = route == tab.route || (route == null && tab.route == Routes.CHATS),
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(Routes.CHATS) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                modifier = Modifier.testTag(tab.tag),
                                icon = {
                                    if (tab.route == Routes.CHATS && unread > 0) {
                                        BadgedBox(badge = { Badge { Text(unread.toString()) } }) { Icon(tab.icon, tab.label) }
                                    } else {
                                        Icon(tab.icon, tab.label)
                                    }
                                },
                                label = { Text(tab.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = c.accent, selectedTextColor = c.accent,
                                    unselectedIconColor = c.muted, unselectedTextColor = c.muted,
                                    indicatorColor = c.surfaceAlt,
                                ),
                            )
                        }
                    }
                }
            },
        ) { inner ->
            NavHost(navController, startDestination = Routes.CHATS, modifier = Modifier.padding(inner)) {
                composable(Routes.CHATS) {
                    ChatsScreen(
                        onOpenChat = { navController.navigate(Routes.chat(it)) },
                        onNewChat = { navController.navigate(Routes.NEW_CHAT) },
                    )
                }
                composable(Routes.CALLS) { CallsScreen() }
                composable(Routes.SETTINGS) { SettingsScreen() }
                composable(
                    Routes.CHAT,
                    arguments = listOf(navArgument("id") { type = NavType.StringType }),
                ) { e -> ChatScreen(chatId = e.arguments?.getString("id").orEmpty()) }
                composable(
                    Routes.NEW_CHAT,
                    enterTransition = { slideInVertically { it } },
                    popExitTransition = { slideOutVertically { it } },
                ) {
                    NewChatScreen(
                        onClose = { navController.popBackStack() },
                        onOpenChat = { id ->
                            navController.navigate(Routes.chat(id)) { popUpTo(Routes.NEW_CHAT) { inclusive = true } }
                        },
                    )
                }
            }
        }
    }
}
