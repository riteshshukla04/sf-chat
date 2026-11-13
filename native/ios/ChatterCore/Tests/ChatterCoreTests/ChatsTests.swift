import XCTest
@testable import ChatterCore

// Port of src/lib/__tests__/chats.test.ts. Thursday 12 November 2026, 15:30 in a fixed UTC calendar.
private var cal: Calendar = {
    var c = Calendar(identifier: .gregorian)
    c.timeZone = TimeZone(identifier: "UTC")!
    return c
}()

private func at(_ day: Int, _ hour: Int, _ minute: Int = 0) -> Int64 {
    let d = cal.date(from: DateComponents(year: 2026, month: 11, day: day, hour: hour, minute: minute))!
    return Int64(d.timeIntervalSince1970 * 1000)
}
private let NOW = at(12, 15, 30)

private let contacts: [String: Contact] = [
    "asha": Contact(id: "asha", name: "Asha Rao", about: "", lastSeenAt: nil),
    "ben": Contact(id: "ben", name: "Ben Okafor", about: "", lastSeenAt: at(12, 9, 5)),
    "chen": Contact(id: "chen", name: "Chen Wei", about: "", lastSeenAt: at(9, 20)),
]

private func chat(_ id: String, kind: ChatKind = .direct, title: String? = nil, members: [String]? = nil, messages: [Message] = [], unread: Int = 0, pinned: Bool = false, muted: Bool = false) -> Chat {
    Chat(id: id, kind: kind, title: title ?? id, members: members ?? [id], messages: messages, unread: unread, pinned: pinned, muted: muted, createdAt: at(1, 9))
}
private func msg(_ id: String, _ from: String, _ text: String, _ sentAt: Int64) -> Message {
    Message(id: id, from: from, text: text, sentAt: sentAt, status: .read)
}

final class FormatTimeTests: XCTestCase {
    func testTodayShowsClockThenYesterdayWeekdayAndDate() {
        XCTAssertEqual(formatTime(at(12, 9, 7), NOW, calendar: cal), "09:07")
        XCTAssertEqual(formatTime(at(11, 23, 59), NOW, calendar: cal), "Yesterday")
        XCTAssertEqual(formatTime(at(9, 12), NOW, calendar: cal), "Monday")
        XCTAssertEqual(formatTime(at(2, 12), NOW, calendar: cal), "02/11/2026")
    }
    func testDayHeadersSayTodayInsteadOfAClock() {
        XCTAssertEqual(dayLabel(at(12, 8), NOW, calendar: cal), "Today")
        XCTAssertEqual(dayLabel(at(11, 8), NOW, calendar: cal), "Yesterday")
    }
    func testFormatClock() {
        XCTAssertEqual(formatClock(at(12, 9, 7), calendar: cal), "09:07")
    }
}

final class ChatListTests: XCTestCase {
    func testPinnedFirstThenMostRecentActivity() {
        let old = chat("old", messages: [msg("1", "ben", "hi", at(10, 9))])
        let recent = chat("recent", messages: [msg("2", "asha", "hey", at(12, 14))])
        let pinned = chat("pinned", messages: [msg("3", "chen", "yo", at(3, 9))], pinned: true)
        XCTAssertEqual(sortChats([old, recent, pinned]).map(\.id), ["pinned", "recent", "old"])
    }
    func testLastActivityFallsBackToCreatedAt() {
        XCTAssertEqual(lastActivityAt(chat("x")), at(1, 9))
    }
    func testPreviewsSayWhoWroteTheLastMessage() {
        XCTAssertEqual(previewText(chat("a", messages: [msg("1", "me", "On my way", NOW)]), contacts), "You: On my way")
        XCTAssertEqual(previewText(chat("g", kind: .group, members: ["asha", "ben"], messages: [msg("1", "ben", "Pizza?", NOW)]), contacts), "Ben: Pizza?")
        XCTAssertEqual(previewText(chat("b", messages: [msg("1", "ben", "Sure", NOW)]), contacts), "Sure")
        XCTAssertEqual(previewText(chat("new"), contacts), "No messages yet")
        XCTAssertEqual(previewText(chat("g", kind: .group, members: ["asha"]), contacts), "Group created. Say hi!")
    }
    func testFirstName() {
        XCTAssertEqual(firstName("Asha Rao"), "Asha")
        XCTAssertEqual(firstName("Cher"), "Cher")
    }
    func testUnreadBadgeLeavesMutedChatsOut() {
        XCTAssertEqual(totalUnread([chat("a", unread: 2), chat("b", unread: 3, muted: true), chat("c", unread: 1)]), 3)
    }
    func testSearchMatchesTitlesAndMessageText() {
        let chats = [chat("Asha Rao"), chat("Ben", messages: [msg("1", "ben", "Movie tonight?", NOW)])]
        XCTAssertEqual(searchChats(chats, "asha").map(\.id), ["Asha Rao"])
        XCTAssertEqual(searchChats(chats, "MOVIE").map(\.id), ["Ben"])
        XCTAssertEqual(searchChats(chats, "  ").count, 2)
    }
}

final class ConversationTests: XCTestCase {
    func testDirectChatSaysWhenContactWasLastSeen() {
        XCTAssertEqual(headerSubtitle(chat("asha"), contacts, NOW, calendar: cal), "online")
        XCTAssertEqual(headerSubtitle(chat("ben"), contacts, NOW, calendar: cal), "last seen today at 09:05")
        XCTAssertEqual(headerSubtitle(chat("chen"), contacts, NOW, calendar: cal), "last seen monday")
    }
    func testJustSeen() {
        var c = contacts
        c["ben"]!.lastSeenAt = NOW - 30_000
        XCTAssertEqual(headerSubtitle(chat("ben"), c, NOW, calendar: cal), "last seen just now")
    }
    func testGroupListsMembersAndLastMessage() {
        let group = chat("g", kind: .group, title: "Flat 4B", members: ["asha", "ben"], messages: [msg("1", "asha", "Rent paid", at(12, 10, 15))])
        XCTAssertEqual(headerSubtitle(group, contacts, NOW, calendar: cal), "Asha, Ben, You · last message 10:15")
    }
    func testGroupWithNoMessagesStillHasSubtitle() {
        let group = chat("g", kind: .group, title: "Weekend trip", members: ["asha", "ben"])
        XCTAssertEqual(headerSubtitle(group, contacts, NOW, calendar: cal), "Asha, Ben, You")
    }
    func testSending() {
        XCTAssertFalse(canSend("   "))
        XCTAssertTrue(canSend(" hi "))
        XCTAssertTrue(canSend(String(repeating: "x", count: 4000)))
        XCTAssertFalse(canSend(String(repeating: "x", count: 4001)))
        let sent = withMessage(chat("a", unread: 4), draftMessage("  Hello  ", NOW, "m1"))
        XCTAssertEqual(sent.messages, [Message(id: "m1", from: "me", text: "Hello", sentAt: NOW, status: .sending)])
        XCTAssertEqual(sent.unread, 0)
    }
    func testTicksMoveOnOneStepAtATime() {
        XCTAssertEqual(advanceStatus(.sending), .sent)
        XCTAssertEqual(advanceStatus(.sent), .delivered)
        XCTAssertEqual(advanceStatus(.delivered), .read)
        XCTAssertEqual(advanceStatus(.read), .read)
    }
    func testMessagesGroupedUnderDayHeaders() {
        let groups = groupByDay([msg("1", "me", "a", at(11, 9)), msg("2", "ben", "b", at(11, 10)), msg("3", "me", "c", at(12, 9))], NOW, calendar: cal)
        XCTAssertEqual(groups.map(\.day), ["Yesterday", "Today"])
        XCTAssertEqual(groups.map(\.messages.count), [2, 1])
    }
    func testAvatarsShowInitials() {
        XCTAssertEqual(initials("Asha Rao"), "AR")
        XCTAssertEqual(initials("Weekend trip ⛺"), "WT")
        XCTAssertEqual(initials(""), "?")
    }
}

final class ReactionTests: XCTestCase {
    private let base = msg("m1", "asha", "hi", at(12, 9))

    func testAddsAReactionAndSummarisesIt() {
        let m = toggleReaction(base, "❤️")
        XCTAssertEqual(reactionSummary(m), [ReactionPill(emoji: "❤️", count: 1, mine: true)])
    }
    func testTappingOwnReactionAgainRemovesIt() {
        let m = toggleReaction(toggleReaction(base, "👍"), "👍")
        XCTAssertNil(m.reactions)
        XCTAssertEqual(reactionSummary(m), [])
    }
    func testDifferentEmojiReplacesMineOthersCounted() {
        var m = toggleReaction(base, "👍", by: "asha")
        m = toggleReaction(m, "👍")
        XCTAssertEqual(reactionSummary(m), [ReactionPill(emoji: "👍", count: 2, mine: true)])
        m = toggleReaction(m, "🙏")
        XCTAssertEqual(reactionSummary(m), [ReactionPill(emoji: "👍", count: 1, mine: false), ReactionPill(emoji: "🙏", count: 1, mine: true)])
    }
    func testIgnoresUnknownEmojisAndDoesNotMutate() {
        XCTAssertEqual(toggleReaction(base, "🔥"), base)
        _ = toggleReaction(base, "👍")
        XCTAssertNil(base.reactions)
    }
}
