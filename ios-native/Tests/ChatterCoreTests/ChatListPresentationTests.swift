import XCTest
@testable import ChatterCore

private let NOW: Millis = 1_794_000_000_000
private let contacts: [String: Contact] = [
    "asha": Contact(id: "asha", name: "Asha Rao", about: "", lastSeenAt: nil),
    "ben": Contact(id: "ben", name: "ben Okafor", about: "", lastSeenAt: nil),
    "chen": Contact(id: "chen", name: "Chen Wei", about: "", lastSeenAt: nil),
]
private func msg(_ from: String, _ status: MessageStatus, at: Millis = NOW - 1000) -> Message {
    Message(id: "m\(at)", from: from, text: "hi", sentAt: at, status: status)
}
private func chat(_ id: String, messages: [Message] = [], unread: Int = 0, pinned: Bool = false,
                  muted: Bool = false, kind: ChatKind = .direct) -> Chat {
    Chat(id: id, kind: kind, title: id, members: [id], messages: messages, unread: unread,
         pinned: pinned, muted: muted, createdAt: NOW - 100_000)
}

final class ChatListPresentationTests: XCTestCase {
    func testAvatarColourMatchesRNHash() {
        // Hand-computed with the RN formula: "A" = 65 → 65 % 8 = 1; "AB" = 65*31+66 = 2081 → 1.
        XCTAssertEqual(avatarColour("A"), avatarColours[1])
        XCTAssertEqual(avatarColour("AB"), avatarColours[2081 % 8])
        XCTAssertEqual(avatarColour(""), avatarColours[0])
        XCTAssertEqual(avatarColour("Asha Rao"), avatarColour("Asha Rao"))
    }

    func testAvatarColourAlwaysInPalette() {
        for n in ["Weekend trip", "😀 party", "Zoë", "x"] { XCTAssertTrue(avatarColours.contains(avatarColour(n))) }
    }

    func testAvatarFontScale() {
        XCTAssertEqual(avatarFontScale(group: true), 0.34)
        XCTAssertEqual(avatarFontScale(group: false), 0.38)
    }

    func testStatusPrefix() {
        func prefix(_ m: Message?) -> String {
            chatRowModel(chat("a", messages: m.map { [$0] } ?? []), contacts: contacts, now: NOW).statusPrefix
        }
        XCTAssertEqual(prefix(msg("me", .read)), "✓✓ ")
        XCTAssertEqual(prefix(msg("me", .sending)), "🕓 ")
        XCTAssertEqual(prefix(msg("me", .delivered)), "✓ ")
        XCTAssertEqual(prefix(msg("me", .sent)), "✓ ")
        XCTAssertEqual(prefix(msg("a", .read)), "")
        XCTAssertEqual(prefix(nil), "")
    }

    func testRowModelFlags() {
        let r = chatRowModel(chat("a", messages: [msg("a", .read)], unread: 3, pinned: true), contacts: contacts, now: NOW)
        XCTAssertTrue(r.timeHighlighted); XCTAssertTrue(r.pinned); XCTAssertEqual(r.unread, 3)
        XCTAssertEqual(r.preview, "hi")
        let muted = chatRowModel(chat("a", unread: 3, muted: true), contacts: contacts, now: NOW)
        XCTAssertFalse(muted.timeHighlighted); XCTAssertTrue(muted.muted)
        XCTAssertFalse(chatRowModel(chat("a"), contacts: contacts, now: NOW).timeHighlighted)
        XCTAssertTrue(chatRowModel(chat("g", kind: .group), contacts: contacts, now: NOW).isGroup)
    }

    func testRowsSortedAndFiltered() {
        let chats = [chat("old", messages: [msg("old", .read, at: NOW - 5000)]),
                     chat("new", messages: [msg("new", .read, at: NOW - 1000)]),
                     chat("pin", messages: [msg("pin", .read, at: NOW - 9000)], pinned: true)]
        XCTAssertEqual(chatRows(chats, contacts: contacts, query: "", now: NOW).map(\.id), ["pin", "new", "old"])
        XCTAssertEqual(chatRows(chats, contacts: contacts, query: " OLD ", now: NOW).map(\.id), ["old"])
        XCTAssertTrue(chatRows(chats, contacts: contacts, query: "zzz", now: NOW).isEmpty)
    }

    func testEmptyText() { XCTAssertEqual(emptySearchText("zz"), "No chats match “zz”") }

    func testSortedContactsByName() {
        XCTAssertEqual(sortedContacts(contacts).map(\.id), ["asha", "ben", "chen"])
    }
}
