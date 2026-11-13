package dev.softwarefactory.chatter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import dev.softwarefactory.chatter.domain.buildSeed
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.AppRoot
import dev.softwarefactory.chatter.ui.NavActions
import org.junit.Rule
import org.junit.Test

/** Compose UI test setup (run with ./gradlew connectedDebugAndroidTest on a device). */
class TabShellTest {
    @get:Rule val rule = createComposeRule()

    private val seed = buildSeed(1_800_000_000_000)
    private val state = AppState(true, contacts = seed.contacts, chats = seed.chats, calls = seed.calls)

    @Test fun tabsAndChatRowsAreShown() {
        rule.setContent { AppRoot(state, null, {}, NavActions({ "x" }, {}, { 1_800_000_000_000 })) }
        rule.onNodeWithTag("tab-chats").assertIsDisplayed()
        rule.onNodeWithTag("chat-row-family").assertIsDisplayed()
        rule.onNodeWithTag("tab-calls").performClick()
        rule.onNodeWithTag("tab-settings").performClick()
    }

    @Test fun hydrationFailureShowsTheErrorScreen() {
        rule.setContent { AppRoot(AppState(), "boom", {}, NavActions({ "x" }, {}, { 0 })) }
        rule.onNodeWithTag("error-screen").assertIsDisplayed()
        rule.onNodeWithTag("error-message").assertIsDisplayed()
        rule.onNodeWithTag("error-retry").assertIsDisplayed()
    }
}
