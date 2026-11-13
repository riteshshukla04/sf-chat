import XCTest
@testable import ChatterCore

final class SeedTests: XCTestCase {
    func testSeedMatchesTheRNSampleData() {
        let now: Millis = 1_800_000_000_000
        let seed = buildSeed(now: now)
        XCTAssertEqual(seed.chats.map(\.id), ["family", "asha", "weekend", "ben", "flat", "chen"])
        XCTAssertEqual(seed.chats.map { $0.messages.count }, [4, 4, 0, 2, 4, 2])
        XCTAssertEqual(seed.chats.flatMap(\.messages).map(\.id), (1...16).map { "seed-\($0)" })
        XCTAssertEqual(seed.chats[0].pinned, true)
        XCTAssertEqual(seed.chats[4].muted, true)
        XCTAssertEqual(seed.chats[3].messages[1].status, .delivered)
        XCTAssertEqual(seed.contacts["ben"]!.lastSeenAt, now - 25 * 60_000)
        XCTAssertNil(seed.contacts["eli"]!.lastSeenAt)
        XCTAssertEqual(seed.calls.map(\.id), ["c1", "c2", "c3", "c4"])
        XCTAssertEqual(seed.calls.map(\.direction), [.missed, .outgoing, .incoming, .incoming])
        XCTAssertEqual(totalUnread(seed.chats), 3)
    }
}
