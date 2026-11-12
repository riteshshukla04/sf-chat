import Foundation

/// What is actually running. Mirrors src/services/build-info.ts for a binary with no OTA updates.
struct BuildInfo: Equatable {
    static let appID = "dev.softwarefactory.chatter"

    var appVersion = "1.0.0"
    var channel = "chat"
    var updateLabel = "embedded"
    var platform = "ios"

    /// Sentry release, e.g. dev.softwarefactory.chatter@1.0.0
    var release: String { "\(Self.appID)@\(appVersion)" }
    /// Sentry dist: the update id or "embedded".
    var dist: String { updateLabel }

    static let current = BuildInfo()

    /// What's new in this build: the `ReleaseNote` Info.plist key, fed by the RELEASE_NOTE build
    /// setting (set it from EXPO_PUBLIC_RELEASE_NOTE). Nil when empty or unset.
    static var releaseNote: String? { releaseNote(from: Bundle.main.object(forInfoDictionaryKey: "ReleaseNote") as? String) }

    static func releaseNote(from value: String?) -> String? {
        guard let value, !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty, !value.hasPrefix("$(") else { return nil }
        return value
    }
}
