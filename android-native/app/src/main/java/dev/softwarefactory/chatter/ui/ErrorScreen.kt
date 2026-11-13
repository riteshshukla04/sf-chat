package dev.softwarefactory.chatter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.ui.theme.Palette
import dev.softwarefactory.chatter.ui.theme.Spacing

/** Port of the root ErrorBoundary UI (src/app/_layout.tsx): shown when startup (hydration) fails. */
@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    val c = Palette.light
    Column(
        Modifier.fillMaxSize().background(c.background).padding(Spacing.xl).testTag(TestTags.ErrorScreen),
        verticalArrangement = Arrangement.spacedBy(Spacing.md, androidx.compose.ui.Alignment.CenterVertically),
    ) {
        Text("Something went wrong", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.text)
        Text(message, color = c.text, modifier = Modifier.testTag(TestTags.ErrorMessage))
        Button(onClick = onRetry, modifier = Modifier.testTag(TestTags.ErrorRetry)) { Text("Try again") }
    }
}
