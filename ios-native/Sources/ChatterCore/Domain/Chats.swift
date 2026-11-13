import Foundation

// Port of src/lib/chats.ts. Pure logic: no UI, no storage, no network.
// Times are epoch milliseconds (`Millis`), like the JS `number`s in the saved JSON.

public typealias Millis = Int64

public enum MessageStatus: String, Codable, Equatable {
    case sending, sent, delivered, read
}

public struct Message: Codable, Equatable {
    public var id: String
    /// "me" for my own messages, otherwise a contact id.
    public var from: String
    public var text: String
    public var sentAt: Millis
    public var status: MessageStatus

    public init(id: String, from: String, text: String, sentAt: Millis, status: MessageStatus) {
        self.id = id; self.from = from; self.text = text; self.sentAt = sentAt; self.status = status
    }
}

public struct Contact: Codable, Equatable {
    public var id: String
    public var name: String
    public var about: String
    /// Epoch ms the contact was last online, or nil when online now (saved as JSON `null`, like the RN app).
    public var lastSeenAt: Millis?

    public init(id: String, name: String, about: String, lastSeenAt: Millis?) {
        self.id = id; self.name = name; self.about = about; self.lastSeenAt = lastSeenAt
    }

    private enum CodingKeys: String, CodingKey { case id, name, about, lastSeenAt }

    public func encode(to encoder: Encoder) throws {
        var c = encoder.container(keyedBy: CodingKeys.self)
        try c.encode(id, forKey: .id)
        try c.encode(name, forKey: .name)
        try c.encode(about, forKey: .about)
        if let lastSeenAt { try c.encode(lastSeenAt, forKey: .lastSeenAt) } else { try c.encodeNil(forKey: .lastSeenAt) }
    }
}

public enum ChatKind: String, Codable, Equatable {
    case direct, group
}

public struct Chat: Codable, Equatable {
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
    public var createdAt: Millis

    public init(id: String, kind: ChatKind, title: String, members: [String], messages: [Message] = [],
                unread: Int = 0, pinned: Bool = false, muted: Bool = false, createdAt: Millis) {
        self.id = id; self.kind = kind; self.title = title; self.members = members; self.messages = messages
        self.unread = unread; self.pinned = pinned; self.muted = muted; self.createdAt = createdAt
    }
}

public struct DayGroup: Equatable {
    public var day: String
    public var messages: [Message]
}

private let minute: Millis = 60_000
private let day: Millis = 24 * 60 * minute
private let weekdays = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"]

private func pad(_ n: Int) -> String { n < 10 ? "0\(n)" : String(n) }

private func date(_ ms: Millis) -> Date { Date(timeIntervalSince1970: Double(ms) / 1000) }

/// "14:05" today, "Yesterday", the weekday within a week, else "12/11/2026".
public func formatTime(_ at: Millis, now: Millis, calendar: Calendar = .current) -> String {
    let t = calendar.dateComponents([.year, .month, .day, .hour, .minute, .weekday], from: date(at))
    let startOfToday = Millis((calendar.startOfDay(for: date(now)).timeIntervalSince1970 * 1000).rounded())
    if at >= startOfToday { return "\(pad(t.hour!)):\(pad(t.minute!))" }
    if at >= startOfToday - day { return "Yesterday" }
    if at >= startOfToday - 6 * day { return weekdays[t.weekday! - 1] }
    return "\(pad(t.day!))/\(pad(t.month!))/\(t.year!)"
}

/// Clock time of a message inside a conversation.
public func formatClock(_ at: Millis, calendar: Calendar = .current) -> String {
    let t = calendar.dateComponents([.hour, .minute], from: date(at))
    return "\(pad(t.hour!)):\(pad(t.minute!))"
}

/// When something last happened in a chat: its newest message, or when it was created.
public func lastActivityAt(_ chat: Chat) -> Millis {
    chat.messages.last?.sentAt ?? chat.createdAt
}

/// Pinned chats first, then the most recently active. (Stable for ties, like JS Array.sort.)
public func sortChats(_ chats: [Chat]) -> [Chat] {
    chats.enumerated().sorted { a, b in
        if a.element.pinned != b.element.pinned { return a.element.pinned }
        let (x, y) = (lastActivityAt(a.element), lastActivityAt(b.element))
        if x != y { return x > y }
        return a.offset < b.offset
    }.map(\.element)
}

/// The line under a chat's name in the list.
public func previewText(_ chat: Chat, contacts: [String: Contact]) -> String {
    guard let last = chat.messages.last else { return chat.kind == .group ? "Group created. Say hi!" : "No messages yet" }
    if last.from == "me" { return "You: \(last.text)" }
    if chat.kind == .group { return "\(firstName(contacts[last.from]?.name ?? "Someone")): \(last.text)" }
    return last.text
}

public func firstName(_ name: String) -> String {
    // JS split(' ')[0]: text before the first single space (may be empty).
    name.split(separator: " ", maxSplits: 1, omittingEmptySubsequences: false).first.map(String.init) ?? name
}

/// Total unread messages over all chats that aren't muted (the badge on the Chats tab).
public func totalUnread(_ chats: [Chat]) -> Int {
    chats.filter { !$0.muted }.reduce(0) { $0 + $1.unread }
}

/// Chats whose title, or any message, contains the query (case-insensitive).
public func searchChats(_ chats: [Chat], query: String) -> [Chat] {
    let q = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
    if q.isEmpty { return chats }
    return chats.filter { $0.title.lowercased().contains(q) || $0.messages.contains { $0.text.lowercased().contains(q) } }
}

/// The line under the name in a conversation's header.
public func headerSubtitle(_ chat: Chat, contacts: [String: Contact], now: Millis, calendar: Calendar = .current) -> String {
    if chat.kind == .group {
        let names = chat.members.map { firstName(contacts[$0]?.name ?? "Someone") }
        let members = (names + ["You"]).joined(separator: ", ")
        // The RN version dereferences the last message unconditionally and throws for a group
        // with no messages yet (the "Weekend trip" seed). A native crash is no behaviour to copy,
        // so such a group just omits the "last message" part.
        guard let last = chat.messages.last else { return members }
        return "\(members) · last message \(formatTime(last.sentAt, now: now, calendar: calendar))"
    }
    guard let first = chat.members.first, let contact = contacts[first] else { return "" }
    guard let seen = contact.lastSeenAt else { return "online" }
    if now - seen < minute { return "last seen just now" }
    let label = formatTime(seen, now: now, calendar: calendar)
    return label.contains(":") ? "last seen today at \(label)" : "last seen \(label.lowercased())"
}

/// A new message of mine, not yet sent.
public func draftMessage(_ text: String, now: Millis, id: String) -> Message {
    Message(id: id, from: "me", text: text.trimmingCharacters(in: .whitespacesAndNewlines), sentAt: now, status: .sending)
}

/// Whether a draft can be sent: something other than spaces, at most 4,000 characters.
public func canSend(_ text: String) -> Bool {
    let n = text.trimmingCharacters(in: .whitespacesAndNewlines).utf16.count // JS .length counts UTF-16 units
    return n > 0 && n <= 4000
}

/// The chat with one more message, and my unread count back to zero (I'm looking at it).
public func withMessage(_ chat: Chat, _ message: Message) -> Chat {
    var c = chat
    c.messages.append(message)
    c.unread = 0
    return c
}

/// Moves my message one step on: sending → sent → delivered → read.
public func advanceStatus(_ status: MessageStatus) -> MessageStatus {
    switch status {
    case .sending: return .sent
    case .sent: return .delivered
    default: return .read
    }
}

/// Messages grouped under day headers ("Today", "Yesterday", ...), oldest day first.
public func groupByDay(_ messages: [Message], now: Millis, calendar: Calendar = .current) -> [DayGroup] {
    var groups: [DayGroup] = []
    for message in messages {
        let label = dayLabel(message.sentAt, now: now, calendar: calendar)
        if let i = groups.indices.last, groups[i].day == label { groups[i].messages.append(message) }
        else { groups.append(DayGroup(day: label, messages: [message])) }
    }
    return groups
}

public func dayLabel(_ at: Millis, now: Millis, calendar: Calendar = .current) -> String {
    let label = formatTime(at, now: now, calendar: calendar)
    return label.contains(":") ? "Today" : label
}

/// Initials for an avatar: "Asha Rao" → "AR", "Weekend trip" → "WT".
public func initials(_ name: String) -> String {
    let letters = name.split(whereSeparator: { $0.isWhitespace }).prefix(2).map { String($0.first!).uppercased() }
    let joined = letters.joined()
    return joined.isEmpty ? "?" : joined
}
