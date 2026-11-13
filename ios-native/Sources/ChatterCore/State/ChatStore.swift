import Foundation

// Port of src/state/store.ts: contacts, chats and calls, persisted locally under the same key and
// JSON shape as the RN app ({ profile, contacts, chats, calls }). There is no server: replies are simulated.

public let storageKey = "chatter:v1"

public struct Profile: Codable, Equatable {
    public var name: String
    public var about: String
    public init(name: String, about: String) { self.name = name; self.about = about }
}

public struct AppState: Equatable {
    public var hydrated = false
    public var profile = Profile(name: "You", about: "Available")
    public var contacts: [String: Contact] = [:]
    public var chats: [Chat] = []
    public var calls: [Call] = []
    public init() {}
}

/// What is written to storage: AppState without `hydrated`.
struct SavedState: Codable {
    var profile: Profile
    var contacts: [String: Contact]
    var chats: [Chat]
    var calls: [Call]
}

public let replies = ["👍", "Sounds good!", "Haha 😄", "On it", "Talk later?", "Nice!", "Sure thing"]

public final class ChatStore {
    public private(set) var state = AppState()

    private let storage: KeyValueStorage
    private let scheduler: Scheduler
    private let clock: () -> Millis
    private var listeners: [Int: () -> Void] = [:]
    private var nextListener = 0
    private var counter = 0

    public init(storage: KeyValueStorage = UserDefaultsStorage(),
                scheduler: Scheduler = MainQueueScheduler(),
                clock: @escaping () -> Millis = { Millis((Date().timeIntervalSince1970 * 1000).rounded()) }) {
        self.storage = storage
        self.scheduler = scheduler
        self.clock = clock
    }

    // MARK: observation

    /// Calls `listener` after every state change. Returns a function that unsubscribes.
    @discardableResult
    public func subscribe(_ listener: @escaping () -> Void) -> () -> Void {
        let id = nextListener
        nextListener += 1
        listeners[id] = listener
        return { [weak self] in self?.listeners[id] = nil }
    }

    private func set(persist: Bool = true, _ change: (inout AppState) -> Void) {
        change(&state)
        for listener in listeners.values { listener() }
        if persist && state.hydrated { save() }
    }

    private func save() {
        let saved = SavedState(profile: state.profile, contacts: state.contacts, chats: state.chats, calls: state.calls)
        if let data = try? JSONEncoder().encode(saved), let json = String(data: data, encoding: .utf8) {
            storage.setItem(storageKey, json)
        }
    }

    // MARK: actions

    /// Loads what was saved, or the sample conversations on a first launch.
    /// Saved data that cannot be decoded is treated like no saved data (the seed replaces it).
    public func hydrate(now: Millis? = nil) {
        let now = now ?? clock()
        let saved = storage.getItem(storageKey)
            .flatMap { $0.data(using: .utf8) }
            .flatMap { try? JSONDecoder().decode(SavedState.self, from: $0) }
        if let saved {
            set(persist: false) {
                $0.profile = saved.profile; $0.contacts = saved.contacts; $0.chats = saved.chats; $0.calls = saved.calls
                $0.hydrated = true
            }
        } else {
            let seed = buildSeed(now: now)
            set(persist: true) {
                $0.contacts = seed.contacts; $0.chats = seed.chats; $0.calls = seed.calls
                $0.hydrated = true
            }
        }
    }

    public func chat(id: String) -> Chat? { state.chats.first { $0.id == id } }

    private func replaceChat(_ chat: Chat) {
        set { $0.chats = $0.chats.map { $0.id == chat.id ? chat : $0 } }
    }

    private func newId(_ prefix: String) -> String {
        counter += 1
        return "\(prefix)-\(clock())-\(counter)"
    }

    public func markRead(_ chatId: String) {
        if var chat = chat(id: chatId), chat.unread > 0 {
            chat.unread = 0
            replaceChat(chat)
        }
    }

    /// Sends my message; its ticks move on, and in a direct chat the other person answers.
    public func sendMessage(_ chatId: String, _ text: String, now: Millis? = nil) {
        guard let chat = chat(id: chatId) else { return }
        let message = draftMessage(text, now: now ?? clock(), id: newId("m"))
        replaceChat(withMessage(chat, message))
        let step = { [weak self] in
            guard let self, var current = self.chat(id: chatId) else { return }
            current.messages = current.messages.map { m in
                guard m.id == message.id else { return m }
                var m = m
                m.status = advanceStatus(m.status)
                return m
            }
            self.replaceChat(current)
        }
        scheduler.schedule(afterMillis: 400, step)
        scheduler.schedule(afterMillis: 1200, step)
        scheduler.schedule(afterMillis: 2600, step)
        if chat.kind == .direct {
            scheduler.schedule(afterMillis: 4200) { [weak self] in
                guard let self, var current = self.chat(id: chatId) else { return }
                let reply = Message(id: self.newId("r"), from: chat.members[0],
                                    text: replies[current.messages.count % replies.count],
                                    sentAt: self.clock(), status: .read)
                current.messages.append(reply)
                self.replaceChat(current)
            }
        }
    }

    /// Opens the direct chat with a contact, starting one if there is none yet. Returns its id.
    @discardableResult
    public func startChat(_ contactId: String, now: Millis? = nil) -> String {
        if let existing = state.chats.first(where: { $0.kind == .direct && $0.members.first == contactId }) { return existing.id }
        let chat = Chat(id: newId("chat"), kind: .direct, title: state.contacts[contactId]?.name ?? "Chat",
                        members: [contactId], createdAt: now ?? clock())
        set { $0.chats.insert(chat, at: 0) }
        return chat.id
    }

    public func togglePin(_ chatId: String) {
        if var chat = chat(id: chatId) {
            chat.pinned.toggle()
            replaceChat(chat)
        }
    }

    public func updateProfile(name: String? = nil, about: String? = nil) {
        set {
            if let name { $0.profile.name = name }
            if let about { $0.profile.about = about }
        }
    }

    /// Back to the sample conversations (Settings → Reset chats).
    public func resetChats(now: Millis? = nil) {
        storage.removeItem(storageKey)
        let seed = buildSeed(now: now ?? clock())
        set { $0.contacts = seed.contacts; $0.chats = seed.chats; $0.calls = seed.calls; $0.hydrated = true }
    }
}
