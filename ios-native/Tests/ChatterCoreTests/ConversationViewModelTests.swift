import XCTest
@testable import ChatterCore

final class ConversationViewModelTests: XCTestCase {
    var cal = Calendar(identifier: .gregorian)
    let now: Millis = 1_800_000_000_000
    let contacts = [
        "a": Contact(id: "a", name: "Asha Rao", about: "", lastSeenAt: nil),
        "b": Contact(id: "b", name: "Ben Kim", about: "", lastSeenAt: nil),
    ]

    override func setUp() { cal.timeZone = TimeZone(identifier: "UTC")! }

    func msg(_ id: String, _ from: String, _ at: Millis, _ s: MessageStatus = .read) -> Message {
        Message(id: id, from: from, text: "t\(id)", sentAt: at, status: s)
    }

    func testRowsHaveDayHeadersOldestFirst() {
        let day: Millis = 86_400_000
        let chat = Chat(id: "c", kind: .direct, title: "Asha", members: ["a"],
                        messages: [msg("1", "a", now - 3 * day), msg("2", "me", now - 1000), msg("3", "a", now - 500)], createdAt: 0)
        let rows = ConversationViewModel.rows(chat: chat, contacts: contacts, now: now, calendar: cal)
        XCTAssertEqual(rows.map(\.key).count, 5)
        XCTAssertEqual(rows.last?.key, "3")
        if case .day(_, let label) = rows[2] { XCTAssertEqual(label, "Today") } else { XCTFail("expected day header") }
        if case .day = rows[0] {} else { XCTFail("first row is a day header") }
    }

    func testTicksPerStatus() {
        XCTAssertEqual(ConversationViewModel.tick(.sending), .clock)
        XCTAssertEqual(ConversationViewModel.tick(.sent), .check)
        XCTAssertEqual(ConversationViewModel.tick(.delivered), .checkAll)
        XCTAssertEqual(ConversationViewModel.tick(.read), .checkAll)
    }

    func testMineBubbleHasTickAndReadColourOnlyWhenRead() {
        let chat = Chat(id: "c", kind: .direct, title: "A", members: ["a"], createdAt: 0)
        let delivered = ConversationViewModel.bubble(msg("1", "me", now, .delivered), chat: chat, contacts: contacts, calendar: cal)
        XCTAssertEqual(delivered.tickTestID, "ticks-delivered")
        XCTAssertFalse(delivered.tickIsRead)
        let read = ConversationViewModel.bubble(msg("1", "me", now, .read), chat: chat, contacts: contacts, calendar: cal)
        XCTAssertTrue(read.tickIsRead)
        XCTAssertEqual(read.testID, "message-1")
    }

    func testTheirsBubbleHasNoTick() {
        let chat = Chat(id: "c", kind: .direct, title: "A", members: ["a"], createdAt: 0)
        let b = ConversationViewModel.bubble(msg("1", "a", now), chat: chat, contacts: contacts, calendar: cal)
        XCTAssertNil(b.tick); XCTAssertNil(b.tickTestID); XCTAssertNil(b.senderName); XCTAssertFalse(b.mine)
    }

    func testGroupShowsSenderFirstNameForTheirsOnly() {
        let chat = Chat(id: "g", kind: .group, title: "G", members: ["a", "b"], createdAt: 0)
        XCTAssertEqual(ConversationViewModel.bubble(msg("1", "a", now), chat: chat, contacts: contacts, calendar: cal).senderName, "Asha")
        XCTAssertEqual(ConversationViewModel.bubble(msg("2", "zz", now), chat: chat, contacts: contacts, calendar: cal).senderName, "Someone")
        XCTAssertNil(ConversationViewModel.bubble(msg("3", "me", now), chat: chat, contacts: contacts, calendar: cal).senderName)
    }

    func testClockFormat() {
        let chat = Chat(id: "c", kind: .direct, title: "A", members: ["a"], createdAt: 0)
        let at: Millis = 1_700_000_000_000 // 22:13 UTC
        XCTAssertEqual(ConversationViewModel.bubble(msg("1", "a", at), chat: chat, contacts: contacts, calendar: cal).clock, "22:13")
    }

    func testSubtitleOfEmptyGroupDoesNotNeedLastMessage() {
        let chat = Chat(id: "g", kind: .group, title: "G", members: ["a", "b"], createdAt: 0)
        XCTAssertEqual(ConversationViewModel.subtitle(chat: chat, contacts: contacts, now: now, calendar: cal), "Asha, Ben, You")
        XCTAssertTrue(ConversationViewModel.showsEmptyNotice(chat))
    }

    func testSubtitleDirectOnline() {
        let chat = Chat(id: "c", kind: .direct, title: "A", members: ["a"], createdAt: 0)
        XCTAssertEqual(ConversationViewModel.subtitle(chat: chat, contacts: contacts, now: now, calendar: cal), "online")
    }

    func testMissingChatText() {
        XCTAssertEqual(ConversationViewModel.missingChatText, "This chat doesn't exist any more.")
    }

    func testSendLimit() {
        XCTAssertFalse(canSend("   "))
        XCTAssertTrue(canSend(String(repeating: "a", count: 4000)))
        XCTAssertFalse(canSend(String(repeating: "a", count: 4001)))
    }
}
