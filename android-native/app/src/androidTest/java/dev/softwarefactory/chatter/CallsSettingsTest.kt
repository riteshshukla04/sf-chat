package dev.softwarefactory.chatter

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import dev.softwarefactory.chatter.domain.buildSeed
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.ErrorScreen
import dev.softwarefactory.chatter.ui.screens.CallsScreen
import dev.softwarefactory.chatter.ui.screens.SettingsScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CallsSettingsTest {
    @get:Rule val rule = createComposeRule()
    private val now = 1_800_000_000_000L
    private val seed = buildSeed(now)
    private val state = AppState(true, contacts = seed.contacts, chats = seed.chats, calls = seed.calls)

    @Test fun callsScreenListsCalls() {
        rule.setContent { CallsScreen(state, now) }
        rule.onNodeWithTag("calls-screen").assertIsDisplayed()
        rule.onNodeWithText("Calls").assertIsDisplayed()
    }

    @Test fun settingsShowsBuildInfoAndResets() {
        var reset = 0
        rule.setContent { SettingsScreen(state, onResetChats = { reset++ }) }
        rule.onNodeWithTag("version-row").assertTextContains("built in", substring = true)
        rule.onNodeWithTag("crash-reporting-row").assertTextContains("off (no DSN)", substring = true)
        rule.onNodeWithTag("reset-chats").performClick()
        rule.onNodeWithText("Reset chats?").assertIsDisplayed()
        rule.onNodeWithTag("reset-confirm").performClick()
        assertEquals(1, reset)
    }

    @Test fun profileEditCallsUpdate() {
        var saved: Pair<String, String>? = null
        rule.setContent { SettingsScreen(state, onUpdateProfile = { n, a -> saved = n to a }) }
        rule.onNodeWithTag("edit-profile").performClick()
        rule.onNodeWithTag("profile-name-input").performTextReplacement("Sam")
        rule.onNodeWithTag("profile-save").performClick()
        assertEquals("Sam", saved?.first)
    }

    @Test fun errorScreenRetries() {
        var retried = false
        rule.setContent { ErrorScreen("boom", { retried = true }) }
        rule.onNodeWithText("Something went wrong").assertIsDisplayed()
        rule.onNodeWithTag("error-retry").performClick()
        assertEquals(true, retried)
    }
}
