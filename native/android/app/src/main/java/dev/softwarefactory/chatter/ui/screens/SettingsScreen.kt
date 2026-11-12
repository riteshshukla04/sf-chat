package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.softwarefactory.chatter.AppContainer
import dev.softwarefactory.chatter.services.BuildInfo
import dev.softwarefactory.chatter.services.ErrorReporting
import dev.softwarefactory.chatter.state.Profile
import dev.softwarefactory.chatter.ui.Avatar
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import dev.softwarefactory.chatter.ui.theme.Spacing
import kotlinx.coroutines.launch

/** Port of src/app/(tabs)/settings.tsx. */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val state by AppContainer.store.state.collectAsState()
    val scope = rememberCoroutineScope()
    SettingsContent(
        profile = state.profile,
        info = BuildInfo.get(),
        sentryEnabled = ErrorReporting.enabled,
        onReset = { scope.launch { AppContainer.store.resetChats() } },
        modifier = modifier,
    )
}

@Composable
fun SettingsContent(
    profile: Profile,
    info: BuildInfo,
    sentryEnabled: Boolean,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = ChatterTheme.colors
    var confirming by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().background(c.surface).statusBarsPadding().testTag("settings-screen")) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Text(
                "Settings", color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm).semantics { heading() },
            )
            Row(
                Modifier.fillMaxWidth().padding(Spacing.lg).testTag("profile-block"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                Avatar(profile.name, size = 64.dp)
                Column {
                    Text(profile.name, color = c.text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    Text(profile.about, color = c.muted)
                }
            }
            HorizontalDivider(color = c.border)
            for (row in settingsRows(info, sentryEnabled)) {
                Column(Modifier.then(if (row.tag != null) Modifier.testTag(row.tag) else Modifier)) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(row.label, color = c.text, fontSize = 16.sp)
                        Text(
                            row.value, color = c.muted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End, modifier = Modifier.weight(1f),
                        )
                    }
                    HorizontalDivider(color = c.border)
                }
            }
            Box(Modifier.fillMaxWidth().clickable { confirming = true }.padding(Spacing.lg).testTag("reset-chats")) {
                Text("Reset chats", color = c.danger, fontWeight = FontWeight.SemiBold)
            }
        }
    }
    if (confirming) {
        ResetChatsDialog(onCancel = { confirming = false }, onConfirm = { confirming = false; onReset() })
    }
}

/** The "Reset chats?" confirmation: Cancel, and a destructive Reset. */
@Composable
fun ResetChatsDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
    val c = ChatterTheme.colors
    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = c.surface,
        title = { Text(RESET_TITLE, color = c.text) },
        text = { Text(RESET_MESSAGE, color = c.muted) },
        dismissButton = { TextButton(onClick = onCancel, modifier = Modifier.testTag("reset-cancel")) { Text("Cancel", color = c.accent) } },
        confirmButton = { TextButton(onClick = onConfirm, modifier = Modifier.testTag("reset-confirm")) { Text("Reset", color = c.danger) } },
        modifier = Modifier.testTag("reset-dialog"),
    )
}
