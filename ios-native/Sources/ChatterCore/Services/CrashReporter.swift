import Foundation

/// Crash reporting seam (src/services/sentry.ts). Sentry is optional and off without a DSN.
/// TODO: add the sentry-cocoa SDK as a package dependency and implement `CrashReporter` with it
/// (release/dist/tags from `BuildInfo`) when a DSN is configured. Until then every call is a no-op;
/// this task deliberately adds no third-party dependency.
public protocol CrashReporter {
    var enabled: Bool { get }
    func captureException(_ error: Error)
    func addBreadcrumb(_ message: String, data: [String: String]?)
}

public struct NoOpCrashReporter: CrashReporter {
    public init() {}
    public var enabled: Bool { false }
    public func captureException(_ error: Error) {}
    public func addBreadcrumb(_ message: String, data: [String: String]?) {}
}

public enum CrashReporting {
    /// The DSN comes from the SENTRY_DSN environment variable or the SentryDSN Info.plist key
    /// (RN: EXPO_PUBLIC_SENTRY_DSN). Without one the no-op reporter is used.
    public static func configuredDSN(env: [String: String] = ProcessInfo.processInfo.environment,
                                     bundle: Bundle = .main) -> String? {
        let dsn = env["SENTRY_DSN"] ?? (bundle.object(forInfoDictionaryKey: "SentryDSN") as? String)
        return (dsn?.isEmpty ?? true) ? nil : dsn
    }

    public static func make(dsn: String? = configuredDSN()) -> CrashReporter {
        // A DSN alone changes nothing yet: see the TODO above.
        NoOpCrashReporter()
    }
}
