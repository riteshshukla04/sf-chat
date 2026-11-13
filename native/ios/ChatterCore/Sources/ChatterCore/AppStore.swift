import Foundation
import Observation

public let storageKey = "chatter:v1"

public struct Profile: Codable, Equatable, Sendable {
    public var name: String
    public var about: String
    public init(name: String, about: String) { self.name = name; self.about = about }
}

/// The JSON saved under `chatter:v1`: exactly {profile, contacts, chats, calls}, as in the RN app.
struct PersistedState: Codable {
    var profile: Profile
    var contacts: [String: Contact]
    var chats: [Chat]
    var calls: [Call]
}

let replies = ["👍", "Sounds good!", "Haha 😄", "On it", "Talk later?", "Nice!", "Sure thing"]

/// App state: contacts, chats and calls, persisted on the device. Port of src/state/store.ts.
/// There is no server: replies to your messages are simulated with timers.
/// Use from the main thread (SwiftUI does).
@Observable
public final class AppStore {
    public private(set) var hydrated = false
    public private(set) var profile = Profile(name: "You", about: "Available")
    public private(set) var contacts: [String: Contact] = [:]
    public private(set) var chats: [Chat] = []
    public private(set) var calls: [Call] = []

    @ObservationIgnored private let storage: KeyValueStorage
    @ObservationIgnored private let scheduler: Scheduler
    @ObservationIgnored private let clock: () -> Int64
    @ObservationIgnored private var counter = 0

    public init(
        storage: KeyValueStorage = UserDefaultsStorage(),
        scheduler: Scheduler = MainQueueScheduler(),
        clock: @escaping () -> Int64 = { Int64(Date().timeIntervalSince1970 * 1000) }
    ) {
        self.storage = storage
        self.scheduler = scheduler
        self.clock = clock
    }

    private func save() {
        let persisted = PersistedState(profile: profile, contacts: contacts, chats: chats, calls: calls)
        if let data = try? JSONEncoder().encode(persisted) { storage.setItem(storageKey, data) }
    }

    /// Persists after every change once hydrated.
    private func commit(persist: Bool = true) {
        if persist && hydrated { save() }
    }

    /// Loads what was saved, or the sample conversations on a first launch.
    public func hydrate() {
        if let data = storage.getItem(storageKey), let saved = try? JSONDecoder().decode(PersistedState.self, from: data) {
            profile = saved.profile; contacts = saved.contacts; chats = saved.chats; calls = saved.calls
            hydrated = true
            commit(persist: false)
        } else {
            let seed = buildSeed(clock())
            contacts = seed.contacts; chats = seed.chats; calls = seed.calls
            hydrated = true
            commit()
        }
    }

    public func chatById(_ id: String) -> Chat? {
        chats.first { $0.id == id }
    }

    private func replaceChat(_ chat: Chat) {
        chats = chats.map { $0.id == chat.id ? chat : $0 }
        commit()
    }

    public func markRead(_ chatId: String) {
        if var chat = chatById(chatId), chat.unread > 0 {
            chat.unread = 0
            replaceChat(chat)
        }
    }

    private func newId(_ prefix: String) -> String {
        counter += 1
        return "\(prefix)-\(clock())-\(counter)"
    }

    /// Sends my message; its ticks move on, and in a direct chat the other person may answer.
    public func sendMessage(_ chatId: String, _ text: String) {
        guard let chat = chatById(chatId) else { return }
        let message = draftMessage(text, clock(), newId("m"))
        replaceChat(withMessage(chat, message))
        let step = { [weak self] in
            guard let self, var current = self.chatById(chatId) else { return }
            current.messages = current.messages.map {
                var m = $0
                if m.id == message.id { m.status = advanceStatus(m.status) }
                return m
            }
            self.replaceChat(current)
        }
        scheduler.schedule(afterMilliseconds: 400, step)
        scheduler.schedule(afterMilliseconds: 1200, step)
        scheduler.schedule(afterMilliseconds: 2600, step)
        if chat.kind == .direct, let other = chat.members.first {
            scheduler.schedule(afterMilliseconds: 4200) { [weak self] in
                guard let self, var current = self.chatById(chatId) else { return }
                let reply = Message(id: self.newId("r"), from: other, text: replies[current.messages.count % replies.count], sentAt: self.clock(), status: .read)
                current.messages.append(reply)
                self.replaceChat(current)
            }
        }
    }

    /// Opens the direct chat with a contact, starting one if there is none yet. Returns its id.
    @discardableResult
    public func startChat(_ contactId: String) -> String {
        if let existing = chats.first(where: { $0.kind == .direct && $0.members.first == contactId }) { return existing.id }
        let chat = Chat(id: newId("chat"), kind: .direct, title: contacts[contactId]?.name ?? "Chat", members: [contactId], createdAt: clock())
        chats = [chat] + chats
        commit()
        return chat.id
    }

    /// Adds, switches or removes my reaction on a message; saved with the chat.
    public func reactToMessage(_ chatId: String, _ messageId: String, _ emoji: String) {
        guard var chat = chatById(chatId) else { return }
        chat.messages = chat.messages.map { $0.id == messageId ? toggleReaction($0, emoji) : $0 }
        replaceChat(chat)
    }

    public func togglePin(_ chatId: String) {
        if var chat = chatById(chatId) {
            chat.pinned.toggle()
            replaceChat(chat)
        }
    }

    public func updateProfile(name: String? = nil, about: String? = nil) {
        if let name { profile.name = name }
        if let about { profile.about = about }
        commit()
    }

    /// Back to the sample conversations (Settings → Reset chats).
    public func resetChats() {
        storage.removeItem(storageKey)
        let seed = buildSeed(clock())
        contacts = seed.contacts; chats = seed.chats; calls = seed.calls
        hydrated = true
        commit()
    }
}
