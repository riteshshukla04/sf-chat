import Foundation

// Presentation logic for the Calls tab, the Settings tab and the error screen
// (RN: src/app/(tabs)/calls.tsx, src/app/(tabs)/settings.tsx, ErrorBoundary in src/app/_layout.tsx).
// Free of UI so it is unit-testable.

// MARK: Calls

/// Everything one row of the calls list shows.
public struct CallRowModel: Equatable {
    public var id: String
    public var name: String
    public var missed: Bool
    public var time: String
    /// SF Symbol for the direction (RN: call-made / call-received / call-missed).
    public var directionSymbol: String
    /// SF Symbol for the trailing action (RN: video-outline / phone-outline).
    public var kindSymbol: String
}

public func callRowModel(_ call: Call, contacts: [String: Contact], now: Millis, calendar: Calendar = .current) -> CallRowModel {
    let symbol: String
    switch call.direction {
    case .outgoing: symbol = "phone.arrow.up.right"
    case .incoming: symbol = "phone.arrow.down.left"
    case .missed: symbol = "phone.arrow.down.left"
    }
    return CallRowModel(
        id: call.id, name: contacts[call.contactId]?.name ?? "Unknown", missed: call.direction == .missed,
        time: formatTime(call.at, now: now, calendar: calendar),
        directionSymbol: call.direction == .missed ? "phone.down" : symbol,
        kindSymbol: call.video ? "video" : "phone")
}

/// Rows in stored order (RN renders `calls` as is).
public func callRows(_ calls: [Call], contacts: [String: Contact], now: Millis, calendar: Calendar = .current) -> [CallRowModel] {
    calls.map { callRowModel($0, contacts: contacts, now: now, calendar: calendar) }
}

// MARK: Settings

public struct SettingsRowModel: Equatable {
    public var label: String
    public var value: String
    public var testID: String?
}

/// "1.0.0 (built in)" for the embedded bundle, else the first 8 characters of the update id.
public func versionText(_ info: BuildInfo) -> String {
    "\(info.appVersion) (\(info.updateLabel == "embedded" ? "built in" : String(info.updateLabel.prefix(8))))"
}

public func crashReportingText(enabled: Bool) -> String { enabled ? "Sentry on" : "off (no DSN)" }

/// The rows under the profile header, in RN order.
public func settingsRows(info: BuildInfo, crashReportingEnabled: Bool) -> [SettingsRowModel] {
    [
        SettingsRowModel(label: "Account", value: "Privacy, security", testID: nil),
        SettingsRowModel(label: "Notifications", value: "Message and call tones", testID: nil),
        SettingsRowModel(label: "Storage", value: "Messages are kept on this phone", testID: nil),
        SettingsRowModel(label: "Version", value: versionText(info), testID: "version-row"),
        SettingsRowModel(label: "Update channel", value: info.channel, testID: nil),
        SettingsRowModel(label: "Crash reporting", value: crashReportingText(enabled: crashReportingEnabled), testID: "crash-reporting-row"),
    ]
}

public enum ResetChatsAlert {
    public static let title = "Reset chats?"
    public static let message = "Back to the sample conversations."
    public static let cancel = "Cancel"
    public static let confirm = "Reset"
}

/// A profile edit: names are trimmed and must not be empty (an empty `about` is allowed).
public struct ProfileDraft: Equatable {
    public var name: String
    public var about: String
    public init(name: String, about: String) { self.name = name; self.about = about }
    public init(_ profile: Profile) { self.init(name: profile.name, about: profile.about) }

    public var trimmed: ProfileDraft {
        ProfileDraft(name: name.trimmingCharacters(in: .whitespacesAndNewlines),
                     about: about.trimmingCharacters(in: .whitespacesAndNewlines))
    }
    public var isValid: Bool { !trimmed.name.isEmpty }
}

// MARK: Error screen

public enum ErrorScreenText {
    public static let title = "Something went wrong"
    public static let retry = "Try again"
}

/// Message the error screen shows and reports for an error.
public func errorMessageText(_ error: Error) -> String {
    let text = (error as? LocalizedError)?.errorDescription ?? error.localizedDescription
    return text.isEmpty ? String(describing: error) : text
}
