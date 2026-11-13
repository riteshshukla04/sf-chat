package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CallMade
import androidx.compose.material.icons.outlined.CallMissed
import androidx.compose.material.icons.outlined.CallReceived
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.AppContainer
import dev.softwarefactory.chatter.ui.Avatar
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import dev.softwarefactory.chatter.ui.theme.Spacing
import kotlinx.coroutines.delay

/** Port of src/app/(tabs)/calls.tsx. */
@Composable
fun CallsScreen(modifier: Modifier = Modifier) {
    val state by AppContainer.store.state.collectAsState()
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) { delay(30_000); now = System.currentTimeMillis() }
    }
    CallsContent(callRows(state.calls, state.contacts, now), modifier)
}

@Composable
fun CallsContent(rows: List<CallRow>, modifier: Modifier = Modifier) {
    val c = ChatterTheme.colors
    Column(modifier.fillMaxSize().background(c.surface).statusBarsPadding().testTag("calls-screen")) {
        Text(
            "Calls", color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm).semantics { heading() },
        )
        LazyColumn(Modifier.fillMaxSize()) {
            items(rows, key = { it.id }) { row ->
                val tint = if (row.missed) c.danger else c.accent
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.md).testTag("call-${row.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    Avatar(row.name, size = 44.dp)
                    Column(Modifier.weight(1f)) {
                        Text(row.name, color = if (row.missed) c.danger else c.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                when (row.arrow) {
                                    CallArrow.Made -> Icons.Outlined.CallMade
                                    CallArrow.Received -> Icons.Outlined.CallReceived
                                    CallArrow.Missed -> Icons.Outlined.CallMissed
                                },
                                contentDescription = row.arrow.name.lowercase(), tint = tint, modifier = Modifier.size(15.dp),
                            )
                            Text(row.time, color = c.muted)
                        }
                    }
                    Icon(
                        if (row.video) Icons.Outlined.Videocam else Icons.Outlined.Phone,
                        contentDescription = if (row.video) "video call" else "voice call", tint = c.accent, modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}
