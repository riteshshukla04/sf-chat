import Foundation

/// Local key-value storage (AsyncStorage's role in the RN app). Injectable so tests can use memory.
public protocol KeyValueStorage: AnyObject {
    func getItem(_ key: String) -> String?
    func setItem(_ key: String, _ value: String)
    func removeItem(_ key: String)
}

/// UserDefaults-backed storage for the app.
public final class UserDefaultsStorage: KeyValueStorage {
    private let defaults: UserDefaults
    public init(defaults: UserDefaults = .standard) { self.defaults = defaults }
    public func getItem(_ key: String) -> String? { defaults.string(forKey: key) }
    public func setItem(_ key: String, _ value: String) { defaults.set(value, forKey: key) }
    public func removeItem(_ key: String) { defaults.removeObject(forKey: key) }
}

public final class InMemoryStorage: KeyValueStorage {
    public private(set) var items: [String: String]
    public init(_ items: [String: String] = [:]) { self.items = items }
    public func getItem(_ key: String) -> String? { items[key] }
    public func setItem(_ key: String, _ value: String) { items[key] = value }
    public func removeItem(_ key: String) { items[key] = nil }
}

/// Runs a block after a delay (setTimeout's role). Injectable so tests can drive time by hand.
public protocol Scheduler: AnyObject {
    func schedule(afterMillis: Int, _ block: @escaping () -> Void)
}

/// Real timers on the main queue.
public final class MainQueueScheduler: Scheduler {
    public init() {}
    public func schedule(afterMillis: Int, _ block: @escaping () -> Void) {
        DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(afterMillis), execute: block)
    }
}
