package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.domain.CallDirection
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.components.PersonAvatar
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import dev.softwarefactory.chatter.ui.theme.Spacing

/** Calls tab. Port of src/app/(tabs)/calls.tsx. [now] is injectable for tests. */
@Composable
fun CallsScreen(state: AppState, now: Long = System.currentTimeMillis()) {
    val c = ChatterTheme.colors
    val rows = remember(state.calls, state.contacts, now) { callRows(state.calls, state.contacts, now) }
    Column(Modifier.fillMaxSize().background(c.surface).testTag("calls-screen")) {
        Text(
            "Calls", color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm).semantics { heading() },
        )
        LazyColumn {
            items(rows, key = { it.id }) { row ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.md).testTag("call-row-${row.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    PersonAvatar(row.name, 44.dp)
                    Column(Modifier.weight(1f)) {
                        Text(row.name, color = if (row.missed) c.danger else c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                when (row.direction) {
                                    CallDirection.Outgoing -> Icons.AutoMirrored.Filled.CallMade
                                    CallDirection.Incoming -> Icons.AutoMirrored.Filled.CallReceived
                                    CallDirection.Missed -> Icons.AutoMirrored.Filled.CallMissed
                                },
                                contentDescription = row.direction.name.lowercase(),
                                tint = if (row.missed) c.danger else c.accent,
                                modifier = Modifier.size(15.dp),
                            )
                            Text(row.time, color = c.muted)
                        }
                    }
                    Icon(
                        if (row.video) Icons.Outlined.Videocam else Icons.Outlined.Call,
                        contentDescription = if (row.video) "video call" else "voice call",
                        tint = c.accent, modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}
