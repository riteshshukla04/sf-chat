import XCTest
@testable import ChatterCore

// Port of src/lib/__tests__/chats.test.ts. Thursday 12 November 2026, 15:30 local time.
private let cal = Calendar.current
private func at(_ day: Int, _ hour: Int, _ minute: Int = 0) -> Millis {
    let d = cal.date(from: DateComponents(year: 2026, month: 11, day: day, hour: hour, minute: minute))!
    return Millis(d.timeIntervalSince1970 * 1000)
}
private let NOW = at(12, 15, 30)

private let contacts: [String: Contact] = [
    "asha": Contact(id: "asha", name: "Asha Rao", about: "", lastSeenAt: nil),
    "ben": Contact(id: "ben", name: "Ben Okafor", about: "", lastSeenAt: at(12, 9, 5)),
    "chen": Contact(id: "chen", name: "Chen Wei", about: "", lastSeenAt: at(9, 20)),
]

private func chat(_ id: String, kind: ChatKind = .direct, title: String? = nil, members: [String]? = nil,
                  messages: [Message] = [], unread: Int = 0, pinned: Bool = false, muted: Bool = false) -> Chat {
    Chat(id: id, kind: kind, title: title ?? id, members: members ?? [id], messages: messages,
         unread: unread, pinned: pinned, muted: muted, createdAt: at(1, 9))
}
private func msg(_ id: String, _ from: String, _ text: String, _ sentAt: Millis) -> Message {
    Message(id: id, from: from, text: text, sentAt: sentAt, status: .read)
}

final class ChatsTests: XCTestCase {
    func testFormatTime() {
        XCTAssertEqual(formatTime(at(12, 9, 7), now: NOW), "09:07")
        XCTAssertEqual(formatTime(at(11, 23, 59), now: NOW), "Yesterday")
        XCTAssertEqual(formatTime(at(9, 12), now: NOW), "Monday")
        XCTAssertEqual(formatTime(at(2, 12), now: NOW), "02/11/2026")
        XCTAssertEqual(formatClock(at(12, 9, 7)), "09:07")
    }

    func testDayLabelsSayTodayInsteadOfAClock() {
        XCTAssertEqual(dayLabel(at(12, 8), now: NOW), "Today")
        XCTAssertEqual(dayLabel(at(11, 8), now: NOW), "Yesterday")
    }

    func testPinnedFirstThenMostRecentActivity() {
        let old = chat("old", messages: [msg("1", "ben", "hi", at(10, 9))])
        let recent = chat("recent", messages: [msg("2", "asha", "hey", at(12, 14))])
        let pinned = chat("pinned", messages: [msg("3", "chen", "yo", at(3, 9))], pinned: true)
        XCTAssertEqual(sortChats([old, recent, pinned]).map(\.id), ["pinned", "recent", "old"])
        XCTAssertEqual(lastActivityAt(chat("empty")), at(1, 9))
    }

    func testPreviewsSayWhoWroteTheLastMessage() {
        XCTAssertEqual(previewText(chat("a", messages: [msg("1", "me", "On my way", NOW)]), contacts: contacts), "You: On my way")
        XCTAssertEqual(previewText(chat("g", kind: .group, members: ["asha", "ben"], messages: [msg("1", "ben", "Pizza?", NOW)]), contacts: contacts), "Ben: Pizza?")
        XCTAssertEqual(previewText(chat("g", kind: .group, members: ["asha"], messages: [msg("1", "zed", "Hi", NOW)]), contacts: contacts), "Someone: Hi")
        XCTAssertEqual(previewText(chat("b", messages: [msg("1", "ben", "Sure", NOW)]), contacts: contacts), "Sure")
        XCTAssertEqual(previewText(chat("new"), contacts: contacts), "No messages yet")
        XCTAssertEqual(previewText(chat("g", kind: .group), contacts: contacts), "Group created. Say hi!")
    }

    func testUnreadBadgeLeavesMutedChatsOut() {
        XCTAssertEqual(totalUnread([chat("a", unread: 2), chat("b", unread: 3, muted: true), chat("c", unread: 1)]), 3)
    }

    func testSearchMatchesTitlesAndMessageText() {
        let chats = [chat("Asha Rao"), chat("Ben", messages: [msg("1", "ben", "Movie tonight?", NOW)])]
        XCTAssertEqual(searchChats(chats, query: "asha").map(\.id), ["Asha Rao"])
        XCTAssertEqual(searchChats(chats, query: "MOVIE").map(\.id), ["Ben"])
        XCTAssertEqual(searchChats(chats, query: "  ").count, 2)
    }

    func testDirectChatSaysWhenContactWasLastSeen() {
        XCTAssertEqual(headerSubtitle(chat("asha"), contacts: contacts, now: NOW), "online")
        XCTAssertEqual(headerSubtitle(chat("ben"), contacts: contacts, now: NOW), "last seen today at 09:05")
        XCTAssertEqual(headerSubtitle(chat("chen"), contacts: contacts, now: NOW), "last seen monday")
        XCTAssertEqual(headerSubtitle(chat("nobody"), contacts: contacts, now: NOW), "")
        var justNow = contacts
        justNow["ben"]!.lastSeenAt = NOW - 30_000
        XCTAssertEqual(headerSubtitle(chat("ben"), contacts: justNow, now: NOW), "last seen just now")
    }

    func testGroupListsMembersAndLastMessage() {
        let group = chat("g", kind: .group, title: "Flat 4B", members: ["asha", "ben"], messages: [msg("1", "asha", "Rent paid", at(12, 10, 15))])
        XCTAssertEqual(headerSubtitle(group, contacts: contacts, now: NOW), "Asha, Ben, You · last message 10:15")
    }

    func testGroupWithNoMessagesDoesNotCrash() {
        // RN throws here; native keeps the screen alive (see headerSubtitle).
        let group = chat("g", kind: .group, members: ["asha", "ben"])
        XCTAssertEqual(headerSubtitle(group, contacts: contacts, now: NOW), "Asha, Ben, You")
    }

    func testSending() {
        XCTAssertFalse(canSend("   "))
        XCTAssertTrue(canSend(" hi "))
        XCTAssertFalse(canSend(String(repeating: "x", count: 4001)))
        XCTAssertTrue(canSend(String(repeating: "x", count: 4000)))
        let sent = withMessage(chat("a", unread: 4), draftMessage("  Hello  ", now: NOW, id: "m1"))
        XCTAssertEqual(sent.messages, [Message(id: "m1", from: "me", text: "Hello", sentAt: NOW, status: .sending)])
        XCTAssertEqual(sent.unread, 0)
    }

    func testTicksMoveOneStepAtATime() {
        XCTAssertEqual(advanceStatus(.sending), .sent)
        XCTAssertEqual(advanceStatus(.sent), .delivered)
        XCTAssertEqual(advanceStatus(.delivered), .read)
        XCTAssertEqual(advanceStatus(.read), .read)
    }

    func testMessagesAreGroupedUnderDayHeaders() {
        let groups = groupByDay([msg("1", "me", "a", at(11, 9)), msg("2", "ben", "b", at(11, 10)), msg("3", "me", "c", at(12, 9))], now: NOW)
        XCTAssertEqual(groups.map { [$0.day, String($0.messages.count)] }, [["Yesterday", "2"], ["Today", "1"]])
    }

    func testInitialsAndFirstName() {
        XCTAssertEqual(initials("Asha Rao"), "AR")
        XCTAssertEqual(initials("Weekend trip ⛺"), "WT")
        XCTAssertEqual(initials(""), "?")
        XCTAssertEqual(firstName("Asha Rao"), "Asha")
        XCTAssertEqual(firstName("Cher"), "Cher")
    }

    func testPaletteAndSpacingMatchTheRNTheme() {
        XCTAssertEqual(Palette.light.accent, "#1A73E8")
        XCTAssertEqual(Palette.dark.bubbleMine, "#0F5E44")
        XCTAssertEqual(Palette.dark.danger, "#FF6B66")
        XCTAssertEqual([Spacing.xs, Spacing.sm, Spacing.md, Spacing.lg, Spacing.xl], [4, 8, 12, 16, 24])
    }
}
