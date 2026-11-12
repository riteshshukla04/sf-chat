import Foundation
#if canImport(Sentry)
import Sentry
#endif

/// Crash reporting. Only started when a DSN was supplied at build time (SENTRY_DSN, surfaced through
/// Info.plist key "SentryDSN"); otherwise everything here is a no-op and no network request is made.
enum SentryService {
    private(set) static var started = false

    /// Returns the DSN, or nil when unset/blank/unsubstituted.
    static func dsn(from info: [String: Any]? = Bundle.main.infoDictionary) -> String? {
        guard let raw = (info?["SentryDSN"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines),
              !raw.isEmpty, !raw.hasPrefix("$(") else { return nil }
        return raw
    }

    static var enabled: Bool { dsn() != nil }

    static func start(info: BuildInfo = .current) {
        guard !started, let dsn = dsn() else { return }
        started = true
        #if canImport(Sentry)
        SentrySDK.start { options in
            options.dsn = dsn
            options.releaseName = info.release
            options.dist = info.dist
            options.environment = info.channel
            options.sendDefaultPii = false
            options.tracesSampleRate = 0.2
            options.debug = false
        }
        SentrySDK.configureScope { scope in
            scope.setTag(value: "chatter", key: "app")
            scope.setTag(value: info.platform, key: "platform")
            scope.setTag(value: info.updateLabel, key: "update_id")
            scope.setTag(value: info.channel, key: "channel")
            scope.setTag(value: info.appVersion, key: "runtime_version")
        }
        #endif
    }

    static func capture(_ error: Error) {
        #if canImport(Sentry)
        if started { SentrySDK.capture(error: error) }
        #endif
    }
}
