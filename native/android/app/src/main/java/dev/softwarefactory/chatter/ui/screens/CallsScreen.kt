package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import dev.softwarefactory.chatter.ui.theme.ChatterTheme


/** STUB: replaced by the CallsScreen task. Keep the signature so the nav graph does not change. */
@Composable
fun CallsScreen(modifier: Modifier = Modifier) {
    val c = ChatterTheme.colors
    Box(modifier.fillMaxSize().background(c.background).testTag("calls-screen"), contentAlignment = Alignment.Center) {
        Text("Calls", color = c.muted)
    }
}
