package dev.softwarefactory.chatter.ui.screens

import dev.softwarefactory.chatter.services.BuildInfo

/** One label/value row of the Settings list. [tag] is the stable test id, if the row has one. */
data class SettingsRow(val label: String, val value: String, val tag: String? = null)

/** "1.0.0 (built in)" for the embedded bundle, otherwise the first 8 characters of the update label. */
fun versionLabel(info: BuildInfo): String =
    "${info.appVersion} (${if (info.updateLabel == "embedded") "built in" else info.updateLabel.take(8)})"

fun crashReportingLabel(sentryEnabled: Boolean): String = if (sentryEnabled) "Sentry on" else "off (no DSN)"

/** The rows under the profile block, in order. */
fun settingsRows(info: BuildInfo, sentryEnabled: Boolean): List<SettingsRow> = listOf(
    SettingsRow("Account", "Privacy, security"),
    SettingsRow("Notifications", "Message and call tones"),
    SettingsRow("Storage", "Messages are kept on this phone"),
    SettingsRow("Version", versionLabel(info), "version-row"),
    SettingsRow("Update channel", info.channel),
    SettingsRow("Crash reporting", crashReportingLabel(sentryEnabled), "crash-reporting-row"),
)

const val RESET_TITLE = "Reset chats?"
const val RESET_MESSAGE = "Back to the sample conversations."
