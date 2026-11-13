import ChatterCore
import XCTest
@testable import Chatter

final class ChatsViewModelTests: XCTestCase {
    private var cal: Calendar = {
        var c = Calendar(identifier: .gregorian)
        c.timeZone = TimeZone(identifier: "UTC")!
        return c
    }()
    private let now: Int64 = 1_700_000_000_000 // 2023-11-14 22:13:20 UTC

    private func msg(_ from: String, _ status: MessageStatus, at: Int64? = nil, text: String = "hi") -> Message {
        Message(id: UUID().uuidString, from: from, text: text, sentAt: at ?? now - 60_000, status: status)
    }
    private func chat(_ id: String, title: String = "T", messages: [Message] = [], unread: Int = 0, pinned: Bool = false, muted: Bool = false, kind: ChatKind = .direct) -> Chat {
        Chat(id: id, kind: kind, title: title, members: ["c1"], messages: messages, unread: unread, pinned: pinned, muted: muted, createdAt: 1)
    }
    private let contacts = ["c1": Contact(id: "c1", name: "Asha Rao", about: "a", lastSeenAt: nil)]

    func testTickPrefix() {
        XCTAssertEqual(ChatsViewModel.tickPrefix(chat("a", messages: [msg("me", .read)])), "✓✓ ")
        XCTAssertEqual(ChatsViewModel.tickPrefix(chat("a", messages: [msg("me", .sending)])), "🕓 ")
        XCTAssertEqual(ChatsViewModel.tickPrefix(chat("a", messages: [msg("me", .sent)])), "✓ ")
        XCTAssertEqual(ChatsViewModel.tickPrefix(chat("a", messages: [msg("me", .delivered)])), "✓ ")
        XCTAssertEqual(ChatsViewModel.tickPrefix(chat("a", messages: [msg("c1", .read)])), "")
        XCTAssertEqual(ChatsViewModel.tickPrefix(chat("a")), "")
    }

    func testRowSubtitleAndTime() {
        let row = ChatsViewModel.row(chat("a", messages: [msg("me", .sent, text: "yo")]), contacts: contacts, now: now, calendar: cal)
        XCTAssertEqual(row.subtitle, "✓ You: yo")
        XCTAssertEqual(row.time, "22:12")
    }

    func testBadgeColourFlags() {
        let unread = ChatsViewModel.row(chat("a", unread: 2), contacts: contacts, now: now)
        XCTAssertTrue(unread.timeIsAccent)
        XCTAssertFalse(unread.badgeIsMuted)
        let muted = ChatsViewModel.row(chat("a", unread: 2, muted: true), contacts: contacts, now: now)
        XCTAssertFalse(muted.timeIsAccent)
        XCTAssertTrue(muted.badgeIsMuted)
        XCTAssertFalse(ChatsViewModel.row(chat("a"), contacts: contacts, now: now).timeIsAccent)
    }

    func testRowsPinnedFirstThenLatest() {
        let chats = [
            chat("old", messages: [msg("c1", .read, at: now - 5000)]),
            chat("new", messages: [msg("c1", .read, at: now - 1000)]),
            chat("pin", messages: [msg("c1", .read, at: now - 9000)], pinned: true),
        ]
        XCTAssertEqual(ChatsViewModel.rows(chats, contacts: contacts, query: "", now: now).map(\.id), ["pin", "new", "old"])
    }

    func testSearchFiltersByTitleAndMessage() {
        let chats = [chat("a", title: "Weekend trip"), chat("b", title: "Work", messages: [msg("c1", .read, text: "lunch?")])]
        XCTAssertEqual(ChatsViewModel.rows(chats, contacts: contacts, query: "TRIP", now: now).map(\.id), ["a"])
        XCTAssertEqual(ChatsViewModel.rows(chats, contacts: contacts, query: "lunch", now: now).map(\.id), ["b"])
        XCTAssertTrue(ChatsViewModel.rows(chats, contacts: contacts, query: "zzz", now: now).isEmpty)
    }

    func testEmptyText() {
        XCTAssertEqual(ChatsViewModel.emptyText(query: "zzz"), "No chats match “zzz”")
    }

    func testSortedContactsByName() {
        let all = [
            "1": Contact(id: "1", name: "Zed", about: "", lastSeenAt: nil),
            "2": Contact(id: "2", name: "amy", about: "", lastSeenAt: nil),
            "3": Contact(id: "3", name: "Bob", about: "", lastSeenAt: nil),
        ]
        XCTAssertEqual(ChatsViewModel.sortedContacts(all).map(\.id), ["2", "3", "1"])
    }

    func testStartChatReusesExistingChat() {
        let store = AppStore(storage: InMemoryStorage(), scheduler: ManualScheduler(), clock: { 1000 })
        store.hydrate()
        let contactId = ChatsViewModel.sortedContacts(store.contacts)[0].id
        let id = store.startChat(contactId)
        XCTAssertEqual(store.startChat(contactId), id)
        XCTAssertNotNil(store.chatById(id))
    }

    func testReleaseNote() {
        XCTAssertNil(BuildInfo.releaseNote(from: nil))
        XCTAssertNil(BuildInfo.releaseNote(from: "  "))
        XCTAssertNil(BuildInfo.releaseNote(from: "$(RELEASE_NOTE)"))
        XCTAssertEqual(BuildInfo.releaseNote(from: "New: reactions"), "New: reactions")
    }
}
