package dev.softwarefactory.chatter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import dev.softwarefactory.chatter.services.BuildInfo
import dev.softwarefactory.chatter.services.buildInfo
import dev.softwarefactory.chatter.state.AppState
import dev.softwarefactory.chatter.ui.components.PersonAvatar
import dev.softwarefactory.chatter.ui.theme.ChatterTheme
import dev.softwarefactory.chatter.ui.theme.Spacing

/** Settings tab. Port of src/app/(tabs)/settings.tsx, plus tapping the profile to edit it (updateProfile). */
@Composable
fun SettingsScreen(
    state: AppState,
    info: BuildInfo = buildInfo(),
    crashReportingEnabled: Boolean = false,
    onUpdateProfile: (name: String, about: String) -> Unit = { _, _ -> },
    onResetChats: () -> Unit = {},
) {
    val c = ChatterTheme.colors
    var confirmReset by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(c.surface).testTag("settings-screen").verticalScroll(rememberScrollState())) {
        Text(
            "Settings", color = c.text, fontSize = 26.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm).semantics { heading() },
        )
        Row(
            Modifier.fillMaxWidth().clickable { editing = true }.padding(Spacing.lg).testTag("edit-profile"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            PersonAvatar(state.profile.name, 64.dp)
            Column {
                Text(state.profile.name, color = c.text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.testTag("profile-name"))
                Text(state.profile.about, color = c.muted, modifier = Modifier.testTag("profile-about"))
            }
        }
        HorizontalDivider(color = c.border)
        settingsRows(info, crashReportingEnabled).forEach { row ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.md).let { if (row.tag != null) it.testTag(row.tag) else it },
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                Text(row.label, color = c.text, fontSize = 16.sp)
                Text(
                    row.value, color = c.muted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End, modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider(color = c.border)
        }
        Text(
            "Reset chats", color = c.danger, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth().clickable { confirmReset = true }.padding(Spacing.lg).testTag("reset-chats"),
        )
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset chats?") },
            text = { Text("Back to the sample conversations.") },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } },
            confirmButton = {
                TextButton(onClick = { confirmReset = false; onResetChats() }, modifier = Modifier.testTag("reset-confirm")) {
                    Text("Reset", color = c.danger)
                }
            },
        )
    }
    if (editing) {
        var name by remember { mutableStateOf(state.profile.name) }
        var about by remember { mutableStateOf(state.profile.about) }
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("Edit profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.testTag("profile-name-input"))
                    OutlinedTextField(about, { about = it }, label = { Text("About") }, singleLine = true, modifier = Modifier.testTag("profile-about-input"))
                }
            },
            dismissButton = { TextButton(onClick = { editing = false }) { Text("Cancel") } },
            confirmButton = {
                TextButton(
                    onClick = {
                        val (n, a) = sanitizeProfile(name, about, state.profile.name to state.profile.about)
                        editing = false
                        onUpdateProfile(n, a)
                    },
                    modifier = Modifier.testTag("profile-save"),
                ) { Text("Save") }
            },
        )
    }
}
