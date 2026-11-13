import Foundation

// Chat logic: pure Swift, no UI, no storage, no network. Port of src/lib/chats.ts.
// All times are epoch milliseconds (Int64), as in the stored JSON.

public enum MessageStatus: String, Codable, Equatable, Sendable {
    case sending, sent, delivered, read
}

public struct Message: Codable, Equatable, Identifiable, Sendable {
    public var id: String
    /// "me" for my own messages, otherwise a contact id.
    public var from: String
    public var text: String
    /// Epoch milliseconds.
    public var sentAt: Int64
    public var status: MessageStatus
    /// emoji → ids of the people who reacted ("me" or contact ids). Absent on old messages.
    public var reactions: [String: [String]]?

    public init(id: String, from: String, text: String, sentAt: Int64, status: MessageStatus, reactions: [String: [String]]? = nil) {
        self.id = id; self.from = from; self.text = text; self.sentAt = sentAt; self.status = status; self.reactions = reactions
    }
}

public let reactionEmojis = ["👍", "❤️", "😂", "😮", "😢", "🙏"]

public struct Contact: Codable, Equatable, Identifiable, Sendable {
    public var id: String
    public var name: String
    public var about: String
    /// Epoch milliseconds the contact was last online, or nil (JSON null) when online now.
    public var lastSeenAt: Int64?

    public init(id: String, name: String, about: String, lastSeenAt: Int64?) {
        self.id = id; self.name = name; self.about = about; self.lastSeenAt = lastSeenAt
    }

    enum CodingKeys: String, CodingKey { case id, name, about, lastSeenAt }

    public init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        id = try c.decode(String.self, forKey: .id)
        name = try c.decode(String.self, forKey: .name)
        about = try c.decode(String.self, forKey: .about)
        lastSeenAt = try c.decodeIfPresent(Int64.self, forKey: .lastSeenAt)
    }

    /// Always writes `lastSeenAt`, as `null` when nil, like the React Native JSON.
    public func encode(to encoder: Encoder) throws {
        var c = encoder.container(keyedBy: CodingKeys.self)
        try c.encode(id, forKey: .id)
        try c.encode(name, forKey: .name)
        try c.encode(about, forKey: .about)
        if let lastSeenAt { try c.encode(lastSeenAt, forKey: .lastSeenAt) } else { try c.encodeNil(forKey: .lastSeenAt) }
    }
}

public enum ChatKind: String, Codable, Equatable, Sendable {
    case direct, group
}

public struct Chat: Codable, Equatable, Identifiable, Sendable {
    public var id: String
    public var kind: ChatKind
    public var title: String
    /// Contact ids (without "me").
    public var members: [String]
    /// Oldest first.
    public var messages: [Message]
    /// How many of the newest messages I haven't read.
    public var unread: Int
    public var pinned: Bool
    public var muted: Bool
    public var createdAt: Int64

    public init(id: String, kind: ChatKind, title: String, members: [String], messages: [Message] = [], unread: Int = 0, pinned: Bool = false, muted: Bool = false, createdAt: Int64) {
        self.id = id; self.kind = kind; self.title = title; self.members = members; self.messages = messages
        self.unread = unread; self.pinned = pinned; self.muted = muted; self.createdAt = createdAt
    }
}

private let minute: Int64 = 60_000
private let day: Int64 = 24 * 60 * minute
private let weekdays = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]

private func pad(_ n: Int) -> String { n < 10 ? "0\(n)" : "\(n)" }

private func date(_ ms: Int64) -> Date { Date(timeIntervalSince1970: Double(ms) / 1000) }

/// "14:05" today, "Yesterday", the weekday within a week, else "12/11/2026".
public func formatTime(_ at: Int64, _ now: Int64, calendar: Calendar = .current) -> String {
    let startOfToday = Int64((calendar.startOfDay(for: date(now)).timeIntervalSince1970 * 1000).rounded())
    if at >= startOfToday { return formatClock(at, calendar: calendar) }
    if at >= startOfToday - day { return "Yesterday" }
    let parts = calendar.dateComponents([.year, .month, .day, .weekday], from: date(at))
    if at >= startOfToday - 6 * day { return weekdays[parts.weekday! - 1] }
    return "\(pad(parts.day!))/\(pad(parts.month!))/\(parts.year!)"
}

/// Clock time of a message inside a conversation.
public func formatClock(_ at: Int64, calendar: Calendar = .current) -> String {
    let parts = calendar.dateComponents([.hour, .minute], from: date(at))
    return "\(pad(parts.hour!)):\(pad(parts.minute!))"
}

/// When something last happened in a chat: its newest message, or when it was created.
public func lastActivityAt(_ chat: Chat) -> Int64 {
    chat.messages.last?.sentAt ?? chat.createdAt
}

/// Pinned chats first, then the most recently active (stable for ties).
public func sortChats(_ chats: [Chat]) -> [Chat] {
    chats.enumerated().sorted { a, b in
        if a.element.pinned != b.element.pinned { return a.element.pinned }
        let ta = lastActivityAt(a.element), tb = lastActivityAt(b.element)
        if ta != tb { return ta > tb }
        return a.offset < b.offset
    }.map(\.element)
}

/// The line under a chat's name in the list.
public func previewText(_ chat: Chat, _ contacts: [String: Contact]) -> String {
    guard let last = chat.messages.last else { return chat.kind == .group ? "Group created. Say hi!" : "No messages yet" }
    if last.from == "me" { return "You: \(last.text)" }
    if chat.kind == .group { return "\(firstName(contacts[last.from]?.name ?? "Someone")): \(last.text)" }
    return last.text
}

public func firstName(_ name: String) -> String {
    name.split(separator: " ", maxSplits: 1, omittingEmptySubsequences: false).first.map(String.init) ?? name
}

/// Total unread messages over all chats that aren't muted (the badge on the Chats tab).
public func totalUnread(_ chats: [Chat]) -> Int {
    chats.filter { !$0.muted }.reduce(0) { $0 + $1.unread }
}

/// Chats whose title, or any message, contains the query (case-insensitive).
public func searchChats(_ chats: [Chat], _ query: String) -> [Chat] {
    let q = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
    if q.isEmpty { return chats }
    return chats.filter { $0.title.lowercased().contains(q) || $0.messages.contains { $0.text.lowercased().contains(q) } }
}

/// The line under the name in a conversation's header.
public func headerSubtitle(_ chat: Chat, _ contacts: [String: Contact], _ now: Int64, calendar: Calendar = .current) -> String {
    if chat.kind == .group {
        let names = chat.members.map { firstName(contacts[$0]?.name ?? "Someone") }
        let members = "\(names.joined(separator: ", ")), You"
        // A brand-new group has no messages yet.
        if let last = chat.messages.last { return "\(members) · last message \(formatTime(last.sentAt, now, calendar: calendar))" }
        return members
    }
    guard let first = chat.members.first, let contact = contacts[first] else { return "" }
    guard let lastSeen = contact.lastSeenAt else { return "online" }
    if now - lastSeen < minute { return "last seen just now" }
    let label = formatTime(lastSeen, now, calendar: calendar)
    return label.contains(":") ? "last seen today at \(label)" : "last seen \(label.lowercased())"
}

/// A new message of mine, not yet sent.
public func draftMessage(_ text: String, _ now: Int64, _ id: String) -> Message {
    Message(id: id, from: "me", text: text.trimmingCharacters(in: .whitespacesAndNewlines), sentAt: now, status: .sending)
}

/// Whether a draft can be sent: something other than spaces, at most 4,000 characters.
public func canSend(_ text: String) -> Bool {
    let trimmed = text.trimmingCharacters(in: .whitespacesAndNewlines)
    return !trimmed.isEmpty && trimmed.utf16.count <= 4000
}

/// The chat with one more message, and my unread count back to zero (I'm looking at it).
public func withMessage(_ chat: Chat, _ message: Message) -> Chat {
    var next = chat
    next.messages.append(message)
    next.unread = 0
    return next
}

/// Moves my message one step on: sending → sent → delivered → read.
public func advanceStatus(_ status: MessageStatus) -> MessageStatus {
    switch status {
    case .sending: return .sent
    case .sent: return .delivered
    default: return .read
    }
}

public struct DayGroup: Equatable {
    public var day: String
    public var messages: [Message]
}

/// Messages grouped under day headers ("Today", "Yesterday", ...), oldest day first.
public func groupByDay(_ messages: [Message], _ now: Int64, calendar: Calendar = .current) -> [DayGroup] {
    var groups: [DayGroup] = []
    for message in messages {
        let label = dayLabel(message.sentAt, now, calendar: calendar)
        if let last = groups.last, last.day == label { groups[groups.count - 1].messages.append(message) }
        else { groups.append(DayGroup(day: label, messages: [message])) }
    }
    return groups
}

public func dayLabel(_ at: Int64, _ now: Int64, calendar: Calendar = .current) -> String {
    let label = formatTime(at, now, calendar: calendar)
    return label.contains(":") ? "Today" : label
}

/// Initials for an avatar: "Asha Rao" → "AR", "Weekend trip" → "WT".
public func initials(_ name: String) -> String {
    let letters = name.split(whereSeparator: { $0.isWhitespace }).prefix(2).compactMap { $0.first.map { String($0).uppercased() } }
    let joined = letters.joined()
    return joined.isEmpty ? "?" : joined
}

/// Toggles a person's reaction on a message. Reacting with the emoji they already used removes it;
/// a different emoji replaces their earlier one (one reaction per person). Unknown emojis are ignored.
public func toggleReaction(_ message: Message, _ emoji: String, by: String = "me") -> Message {
    guard reactionEmojis.contains(emoji) else { return message }
    var next: [String: [String]] = [:]
    let had = message.reactions?[emoji]?.contains(by) ?? false
    for (e, people) in message.reactions ?? [:] {
        let rest = people.filter { $0 != by }
        if !rest.isEmpty { next[e] = rest }
    }
    if !had { next[emoji, default: []].append(by) }
    var result = message
    result.reactions = next.isEmpty ? nil : next
    return result
}

public struct ReactionPill: Equatable {
    public var emoji: String
    public var count: Int
    public var mine: Bool
}

/// Reactions as pills, in the picker's order: emoji, count, and whether I used it.
public func reactionSummary(_ message: Message) -> [ReactionPill] {
    reactionEmojis.compactMap { emoji in
        guard let people = message.reactions?[emoji], !people.isEmpty else { return nil }
        return ReactionPill(emoji: emoji, count: people.count, mine: people.contains("me"))
    }
}
