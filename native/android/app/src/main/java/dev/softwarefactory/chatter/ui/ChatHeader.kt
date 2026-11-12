package dev.softwarefactory.chatter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import dev.softwarefactory.chatter.ui.theme.Spacing

/** Avatar + title + subtitle for the chat top bar. Subtitle carries the `chat-subtitle` test tag. */
@Composable
fun ChatHeader(title: String, subtitle: String, group: Boolean, modifier: Modifier = Modifier) {
    val c = ChatterTheme.colors
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Avatar(name = title, size = 36.dp, group = group)
        Column {
            Text(title, color = c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = c.muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.testTag("chat-subtitle"))
        }
    }
}
