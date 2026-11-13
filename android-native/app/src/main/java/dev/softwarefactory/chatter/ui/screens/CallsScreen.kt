package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.theme.Spacing

/** PLACEHOLDER: the calls task replaces this file. */
@Composable
fun CallsScreen(state: AppState) {
    Column(Modifier.fillMaxSize().padding(Spacing.lg)) {
        Text("Calls (placeholder): ${state.calls.size} calls")
    }
}
