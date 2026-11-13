package dev.softwarefactory.chatter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.ui.theme.Palette
import dev.softwarefactory.chatter.ui.theme.Spacing

/**
 * Port of `ErrorBoundary` in src/app/_layout.tsx. Always uses the light palette, like the original.
 */
@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val c = Palette.light
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(c.background)
            .safeDrawingPadding()
            .padding(Spacing.xl)
            .semantics { testTagsAsResourceId = true }
            .testTag("error-screen"),
        verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
    ) {
        Text("Something went wrong", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = c.text)
        Text(message, color = c.text, modifier = Modifier.testTag("error-message"))
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth().testTag("error-retry"),
            colors = ButtonDefaults.buttonColors(containerColor = c.accent, contentColor = Color.White),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
        ) {
            Text("Try again", fontWeight = FontWeight.SemiBold)
        }
    }
}

private typealias Color = androidx.compose.ui.graphics.Color
