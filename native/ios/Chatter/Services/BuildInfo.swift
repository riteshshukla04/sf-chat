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
}
