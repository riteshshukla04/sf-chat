import SwiftUI

/// Stub. Settings task replaces this file (identifiers: settings-screen, version-row,
/// crash-reporting-row, reset-chats). Use BuildInfo.current and SentryService.enabled.
struct SettingsScreen: View {
    var body: some View {
        Text("Settings").font(.title2.bold()).foregroundColor(Theme.text)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Theme.background)
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("settings-screen")
    }
}
