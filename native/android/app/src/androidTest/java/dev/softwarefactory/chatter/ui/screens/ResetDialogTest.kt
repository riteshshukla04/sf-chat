package dev.softwarefactory.chatter.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dev.softwarefactory.chatter.services.BuildInfo
import dev.softwarefactory.chatter.state.Profile
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ResetDialogTest {
    @get:Rule val rule = createComposeRule()
    private var resets = 0

    private fun show() {
        resets = 0
        rule.setContent {
            ChatterTheme {
                SettingsContent(Profile("You", "Available"), BuildInfo.create("1.0.0"), false, onReset = { resets++ })
            }
        }
    }

    @Test fun showsRows() {
        show()
        rule.onNodeWithTag("settings-screen").assertIsDisplayed()
        rule.onNodeWithTag("version-row").assertIsDisplayed()
        rule.onNodeWithText("1.0.0 (built in)").assertIsDisplayed()
        rule.onNodeWithText("off (no DSN)").assertIsDisplayed()
    }

    @Test fun cancelKeepsChats() {
        show()
        rule.onNodeWithTag("reset-chats").performClick()
        rule.onNodeWithText("Reset chats?").assertIsDisplayed()
        rule.onNodeWithText("Back to the sample conversations.").assertIsDisplayed()
        rule.onNodeWithText("Cancel").performClick()
        rule.onNodeWithTag("reset-dialog").assertDoesNotExist()
        assertEquals(0, resets)
    }

    @Test fun resetConfirms() {
        show()
        rule.onNodeWithTag("reset-chats").performClick()
        rule.onNodeWithText("Reset").performClick()
        rule.onNodeWithTag("reset-dialog").assertDoesNotExist()
        assertEquals(1, resets)
    }
}
