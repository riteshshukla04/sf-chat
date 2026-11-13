import Foundation

/// Port of src/services/build-info.ts. expo-updates (OTA) does not apply to a native app, so there is
/// never an update id: the update label is always "embedded", the channel "none".
public let appId = "dev.softwarefactory.chatter"

public struct BuildInfo: Equatable {
    public var appVersion: String
    public var runtimeVersion: String
    public var channel: String
    public var updateId: String?
    public var updateLabel: String
    public var updatesEnabled: Bool
    public var platform: String
    /// Sentry release, e.g. dev.softwarefactory.chatter@1.0.0
    public var release: String
    /// Sentry dist: "embedded".
    public var dist: String
}

public func getBuildInfo(bundle: Bundle = .main) -> BuildInfo {
    let version = (bundle.infoDictionary?["CFBundleShortVersionString"] as? String) ?? "1.0.0"
    return BuildInfo(appVersion: version, runtimeVersion: version, channel: "none", updateId: nil,
                     updateLabel: "embedded", updatesEnabled: false, platform: "ios",
                     release: "\(appId)@\(version)", dist: "embedded")
}
