package dev.softwarefactory.chatter.ui.screens

import dev.softwarefactory.chatter.services.BuildInfo

/** Text for the Settings rows (port of src/app/(tabs)/settings.tsx). */
fun versionValue(info: BuildInfo): String =
    "${info.appVersion} (${if (info.updateLabel == "embedded") "built in" else info.updateLabel.take(8)})"

fun crashReportingValue(enabled: Boolean): String = if (enabled) "Sentry on" else "off (no DSN)"

/** Profile edits: blank names are rejected (keeps the old one), the about text is trimmed. */
fun sanitizeProfile(name: String, about: String, current: Pair<String, String>): Pair<String, String> =
    (name.trim().ifEmpty { current.first }) to about.trim()

data class SettingsRowModel(val label: String, val value: String, val tag: String? = null)

fun settingsRows(info: BuildInfo, crashEnabled: Boolean): List<SettingsRowModel> = listOf(
    SettingsRowModel("Account", "Privacy, security"),
    SettingsRowModel("Notifications", "Message and call tones"),
    SettingsRowModel("Storage", "Messages are kept on this phone"),
    SettingsRowModel("Version", versionValue(info), "version-row"),
    SettingsRowModel("Update channel", info.channel),
    SettingsRowModel("Crash reporting", crashReportingValue(crashEnabled), "crash-reporting-row"),
)
