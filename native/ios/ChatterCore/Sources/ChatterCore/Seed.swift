import Foundation

// The conversations a fresh install starts with, timed relative to "now". Port of src/data/seed.ts.
// Made-up people; nothing here comes from a real account.

public enum CallDirection: String, Codable, Equatable, Sendable {
    case incoming, outgoing, missed
}

public struct Call: Codable, Equatable, Identifiable, Sendable {
    public var id: String
    public var contactId: String
    public var at: Int64
    public var direction: CallDirection
    public var video: Bool

    public init(id: String, contactId: String, at: Int64, direction: CallDirection, video: Bool) {
        self.id = id; self.contactId = contactId; self.at = at; self.direction = direction; self.video = video
    }
}

public struct Seed: Equatable {
    public var contacts: [String: Contact]
    public var chats: [Chat]
    public var calls: [Call]
}

private let MIN: Int64 = 60_000
private let HOUR: Int64 = 60 * MIN
private let DAY: Int64 = 24 * HOUR

public func buildSeed(_ now: Int64) -> Seed {
    let contacts: [Contact] = [
        Contact(id: "asha", name: "Asha Rao", about: "Busy building things", lastSeenAt: nil),
        Contact(id: "ben", name: "Ben Okafor", about: "At the gym", lastSeenAt: now - 25 * MIN),
        Contact(id: "chen", name: "Chen Wei", about: "Available", lastSeenAt: now - 3 * HOUR),
        Contact(id: "dana", name: "Dana Silva", about: "Hey there! I am using Chatter.", lastSeenAt: now - 2 * DAY),
        Contact(id: "eli", name: "Eli Novak", about: "Coffee first", lastSeenAt: nil),
        Contact(id: "fatima", name: "Fatima Khan", about: "In a meeting", lastSeenAt: now - 50 * MIN),
        Contact(id: "gabe", name: "Gabe Moreau", about: "Travelling", lastSeenAt: now - 5 * DAY),
        Contact(id: "hana", name: "Hana Ito", about: "Urgent calls only", lastSeenAt: now - 9 * MIN),
    ]
    var n = 0
    func m(_ from: String, _ text: String, _ ago: Int64, _ status: MessageStatus = .read) -> Message {
        n += 1
        return Message(id: "seed-\(n)", from: from, text: text, sentAt: now - ago, status: status)
    }

    // Evaluated in order so the seed-N ids match the React Native seed.
    let family = Chat(
        id: "family", kind: .group, title: "Family 🏠", members: ["chen", "dana"],
        messages: [
            m("dana", "Sunday lunch at ours?", 3 * HOUR),
            m("chen", "Yes! I will bring dessert", 2 * HOUR + 40 * MIN),
            m("me", "Count me in 🙌", 2 * HOUR + 30 * MIN),
            m("dana", "Lunch at 1. Bring the photos from the trip", 50 * MIN),
        ],
        unread: 1, pinned: true, muted: false, createdAt: now - 40 * DAY)
    let asha = Chat(
        id: "asha", kind: .direct, title: "Asha Rao", members: ["asha"],
        messages: [
            m("asha", "Did you see the release notes?", 26 * HOUR),
            m("me", "Yes, the new search is great", 25 * HOUR),
            m("asha", "Can you review my PR before 5?", 12 * MIN),
            m("asha", "It's the one about the chat list", 11 * MIN),
        ],
        unread: 2, pinned: false, muted: false, createdAt: now - 90 * DAY)
    // Just created: nobody has written yet.
    let weekend = Chat(
        id: "weekend", kind: .group, title: "Weekend trip ⛺", members: ["asha", "ben", "eli"],
        messages: [], unread: 0, pinned: false, muted: false, createdAt: now - 8 * MIN)
    let ben = Chat(
        id: "ben", kind: .direct, title: "Ben Okafor", members: ["ben"],
        messages: [m("ben", "Leg day tomorrow?", 3 * HOUR), m("me", "Always. 7am?", 2 * HOUR, .delivered)],
        unread: 0, pinned: false, muted: false, createdAt: now - 60 * DAY)
    let flat = Chat(
        id: "flat", kind: .group, title: "Flat 4B", members: ["fatima", "hana", "gabe"],
        messages: [
            m("fatima", "Rent is due Friday", 2 * DAY),
            m("hana", "Paid ✅", 2 * DAY - 3 * HOUR),
            m("gabe", "Paying tonight, sorry!", 1 * DAY + 2 * HOUR),
            m("fatima", "Also who left the oven on 😅", 5 * HOUR),
        ],
        unread: 4, pinned: false, muted: true, createdAt: now - 200 * DAY)
    let chen = Chat(
        id: "chen", kind: .direct, title: "Chen Wei", members: ["chen"],
        messages: [m("me", "Thanks for the book!", 4 * DAY), m("chen", "Enjoy it 📚", 4 * DAY - 20 * MIN)],
        unread: 0, pinned: false, muted: false, createdAt: now - 120 * DAY)

    let calls: [Call] = [
        Call(id: "c1", contactId: "asha", at: now - 40 * MIN, direction: .missed, video: false),
        Call(id: "c2", contactId: "ben", at: now - 5 * HOUR, direction: .outgoing, video: true),
        Call(id: "c3", contactId: "dana", at: now - 1 * DAY - 2 * HOUR, direction: .incoming, video: false),
        Call(id: "c4", contactId: "hana", at: now - 3 * DAY, direction: .incoming, video: true),
    ]

    return Seed(
        contacts: Dictionary(uniqueKeysWithValues: contacts.map { ($0.id, $0) }),
        chats: [family, asha, weekend, ben, flat, chen],
        calls: calls)
}
