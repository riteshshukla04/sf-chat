import XCTest
@testable import ChatterCore

final class ChatRowsTests: XCTestCase {
    var cal: Calendar { var c = Calendar(identifier: .gregorian); c.timeZone = TimeZone(identifier: "UTC")!; return c }
    let now: Int64 = 1_700_000_000_000 // 2023-11-14 22:13 UTC
    let day: Int64 = 86_400_000

    func msg(_ id: String, _ at: Int64) -> Message {
        Message(id: id, from: "me", text: id, sentAt: at, status: .sent)
    }

    func testEmptyHasNoRows() {
        XCTAssertEqual(chatRows([], now, calendar: cal), [])
    }

    func testDayPillsPrecedeTheirMessagesOldestFirst() {
        let rows = chatRows([msg("a", now - day), msg("b", now - 1000), msg("c", now)], now, calendar: cal)
        XCTAssertEqual(rows.map(\.id), ["day-Yesterday", "a", "day-Today", "b", "c"])
        XCTAssertEqual(rows.last, .message(msg("c", now)))
    }

    func testPickerTogglesAndSwitches() {
        var p = PickerState()
        p.longPress("a"); XCTAssertEqual(p.open, "a")
        p.longPress("b"); XCTAssertEqual(p.open, "b")
        p.longPress("b"); XCTAssertNil(p.open)
        p.longPress("a"); p.close(); XCTAssertNil(p.open)
    }
}
