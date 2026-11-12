import Foundation

/// Where the app's single JSON blob lives. Mirrors AsyncStorage's getItem/setItem/removeItem.
public protocol KeyValueStorage: AnyObject {
    func getItem(_ key: String) -> Data?
    func setItem(_ key: String, _ value: Data)
    func removeItem(_ key: String)
}

public final class UserDefaultsStorage: KeyValueStorage {
    private let defaults: UserDefaults
    public init(defaults: UserDefaults = .standard) { self.defaults = defaults }
    public func getItem(_ key: String) -> Data? { defaults.data(forKey: key) }
    public func setItem(_ key: String, _ value: Data) { defaults.set(value, forKey: key) }
    public func removeItem(_ key: String) { defaults.removeObject(forKey: key) }
}

public final class InMemoryStorage: KeyValueStorage {
    public private(set) var items: [String: Data] = [:]
    public init() {}
    public func getItem(_ key: String) -> Data? { items[key] }
    public func setItem(_ key: String, _ value: Data) { items[key] = value }
    public func removeItem(_ key: String) { items[key] = nil }
}

/// Runs work after a delay in milliseconds. Injected so tests control time.
public protocol Scheduler: AnyObject {
    func schedule(afterMilliseconds ms: Int, _ work: @escaping () -> Void)
}

/// Real timers on the main queue.
public final class MainQueueScheduler: Scheduler {
    public init() {}
    public func schedule(afterMilliseconds ms: Int, _ work: @escaping () -> Void) {
        DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(ms), execute: work)
    }
}

/// Test scheduler: nothing runs until `advance(by:)` moves virtual time forward.
public final class ManualScheduler: Scheduler {
    public private(set) var now = 0
    private var pending: [(at: Int, order: Int, work: () -> Void)] = []
    private var order = 0
    public init() {}

    public func schedule(afterMilliseconds ms: Int, _ work: @escaping () -> Void) {
        order += 1
        pending.append((now + ms, order, work))
    }

    public func advance(by ms: Int) {
        let target = now + ms
        while let next = pending.filter({ $0.at <= target }).min(by: { ($0.at, $0.order) < ($1.at, $1.order) }) {
            pending.removeAll { $0.order == next.order }
            now = next.at
            next.work()
        }
        now = target
    }
}
