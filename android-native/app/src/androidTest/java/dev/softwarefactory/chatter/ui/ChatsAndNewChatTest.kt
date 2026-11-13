package dev.softwarefactory.chatter.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import dev.softwarefactory.chatter.domain.Chat
import dev.softwarefactory.chatter.domain.buildSeed
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.screens.ChatsListScreen
import dev.softwarefactory.chatter.ui.screens.NewChatScreen
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ChatsAndNewChatTest {
    @get:Rule val rule = createComposeRule()

    private val now = 1_800_000_000_000
    private val seed = buildSeed(now)
    private val state = AppState(true, contacts = seed.contacts, chats = seed.chats, calls = seed.calls)

    @Test fun rowTapLongPressAndNewChat() {
        var opened: Chat? = null
        var pinned: String? = null
        var newChat = 0
        rule.setContent {
            ChatterTheme { ChatsListScreen(state, { opened = it }, { newChat++ }, { pinned = it }, now) }
        }
        rule.onNodeWithTag("chat-row-asha").assertIsDisplayed().performClick()
        assertEquals("asha", opened?.id)
        rule.onNodeWithTag("chat-row-ben").performTouchInput { longClick() }
        assertEquals("ben", pinned)
        rule.onNodeWithTag("new-chat").performClick()
        assertEquals(1, newChat)
    }

    @Test fun searchFiltersAndShowsEmptyState() {
        rule.setContent { ChatterTheme { ChatsListScreen(state, {}, {}, {}, now) } }
        rule.onNodeWithTag("chat-search").performTextInput("zzzz")
        rule.onNodeWithText("No chats match “zzzz”").assertIsDisplayed()
    }

    @Test fun contactTapStartsChat() {
        var started: String? = null
        rule.setContent { ChatterTheme { NewChatScreen(state, { started = it }, {}) } }
        rule.onNodeWithTag("new-chat-screen").assertIsDisplayed()
        rule.onNodeWithTag("contact-asha").performClick()
        assertEquals("asha", started)
    }
}
