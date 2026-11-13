package dev.softwarefactory.chatter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dev.softwarefactory.chatter.domain.buildSeed
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.screens.ConversationScreen
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import org.junit.Rule
import org.junit.Test

class ConversationScreenTest {
    @get:Rule val rule = createComposeRule()

    private val now = 1_800_000_000_000L
    private val seed = buildSeed(now)
    private val state = AppState(true, contacts = seed.contacts, chats = seed.chats, calls = seed.calls)

    @Test fun showsHeaderSubtitleAndMarksRead() {
        val opened = mutableListOf<String>()
        rule.setContent { ChatterTheme { ConversationScreen("family", state, now, {}, onMarkRead = { opened += it }) } }
        rule.onNodeWithTag("chat-subtitle").assertIsDisplayed()
        rule.runOnIdle { assert(opened == listOf("family")) }
    }

    @Test fun sendButtonFollowsCanSendAndSendsTrimmedDraft() {
        val sent = mutableListOf<Pair<String, String>>()
        rule.setContent { ChatterTheme { ConversationScreen("family", state, now, {}, onSend = { id, t -> sent += id to t }) } }
        rule.onNodeWithTag("send").assertIsNotEnabled()
        rule.onNodeWithTag("message-input").performTextInput("   ")
        rule.onNodeWithTag("send").assertIsNotEnabled()
        rule.onNodeWithTag("message-input").performTextInput("hello")
        rule.onNodeWithTag("send").assertIsEnabled().performClick()
        rule.runOnIdle { assert(sent == listOf("family" to "    hello")) }
    }

    @Test fun missingChatShowsFallback() {
        rule.setContent { ChatterTheme { ConversationScreen("nope", state, now, {}) } }
        rule.onNodeWithText("This chat doesn't exist any more.").assertIsDisplayed()
    }

    @Test fun emptyGroupDoesNotCrashAndShowsNotice() {
        val empty = seed.chats.first { it.messages.isEmpty() }
        rule.setContent { ChatterTheme { ConversationScreen(empty.id, state, now, {}) } }
        rule.onNodeWithText("Messages are kept on this phone.", substring = true).assertIsDisplayed()
        rule.onNodeWithTag("chat-subtitle").assertTextContains(empty.members.size.toString(), substring = true, ignoreCase = true).assertIsDisplayed()
    }
}
