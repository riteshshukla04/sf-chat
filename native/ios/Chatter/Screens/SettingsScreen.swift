import ChatterCore
import SwiftUI

/// Row texts for Settings. Mirrors src/app/(tabs)/settings.tsx.
enum SettingsLogic {
    /// "1.0.0 (built in)" for the embedded bundle, otherwise the first 8 characters of the update label.
    static func versionValue(_ info: BuildInfo) -> String {
        "\(info.appVersion) (\(info.updateLabel == "embedded" ? "built in" : String(info.updateLabel.prefix(8))))"
    }

    static func crashReportingValue(enabled: Bool) -> String {
        enabled ? "Sentry on" : "off (no DSN)"
    }

    static let resetTitle = "Reset chats?"
    static let resetMessage = "Back to the sample conversations."
}

struct SettingsScreen: View {
    @Environment(AppStore.self) private var store
    @State private var confirmReset = false

    private let info = BuildInfo.current

    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                Text("Settings")
                    .font(.system(size: 26, weight: .bold))
                    .foregroundColor(Theme.text)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, Spacing.lg)
                    .padding(.vertical, Spacing.sm)
                    .accessibilityAddTraits(.isHeader)
                profile
                row("Account", "Privacy, security")
                row("Notifications", "Message and call tones")
                row("Storage", "Messages are kept on this phone")
                row("Version", SettingsLogic.versionValue(info), id: "version-row")
                row("Update channel", info.channel)
                row("Crash reporting", SettingsLogic.crashReportingValue(enabled: SentryService.enabled), id: "crash-reporting-row")
                Button { confirmReset = true } label: {
                    Text("Reset chats")
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(Theme.danger)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(Spacing.lg)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("reset-chats")
            }
        }
        .background(Theme.surface)
        .alert(SettingsLogic.resetTitle, isPresented: $confirmReset) {
            Button("Cancel", role: .cancel) {}
            Button("Reset", role: .destructive) { store.resetChats() }
        } message: {
            Text(SettingsLogic.resetMessage)
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("settings-screen")
    }

    private var profile: some View {
        VStack(spacing: 0) {
            HStack(spacing: Spacing.lg) {
                AvatarView(name: store.profile.name, size: 64)
                VStack(alignment: .leading, spacing: 2) {
                    Text(store.profile.name)
                        .font(.system(size: 20, weight: .semibold))
                        .foregroundColor(Theme.text)
                    Text(store.profile.about).foregroundColor(Theme.muted)
                }
                Spacer(minLength: 0)
            }
            .padding(Spacing.lg)
            Divider().overlay(Theme.border)
        }
    }

    @ViewBuilder
    private func row(_ label: String, _ value: String, id: String? = nil) -> some View {
        let content = VStack(spacing: 0) {
            HStack(spacing: Spacing.lg) {
                Text(label).font(.system(size: 16)).foregroundColor(Theme.text)
                Spacer(minLength: 0)
                Text(value)
                    .font(.system(size: 14))
                    .foregroundColor(Theme.muted)
                    .lineLimit(1)
                    .multilineTextAlignment(.trailing)
            }
            .padding(.horizontal, Spacing.lg)
            .padding(.vertical, Spacing.md)
            Divider().overlay(Theme.border)
        }
        if let id {
            content.accessibilityElement(children: .combine).accessibilityIdentifier(id)
        } else {
            content
        }
    }
}
