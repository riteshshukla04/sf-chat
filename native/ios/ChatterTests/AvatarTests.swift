import XCTest
@testable import Chatter

final class AvatarTests: XCTestCase {
    func testHashMatchesReference() {
        // JS reference: hash = (hash * 31 + charCodeAt(0)) >>> 0
        XCTAssertEqual(AvatarLogic.hash(""), 0)
        XCTAssertEqual(AvatarLogic.hash("A"), 65)
        XCTAssertEqual(AvatarLogic.hash("AB"), 65 * 31 + 66)
        // "Asha Rao" computed with the original algorithm (wraps past 2^32 for long names)
        var expected: UInt64 = 0
        for u in "Weekend trip with the whole team".utf16 { expected = (expected * 31 + UInt64(u)) % (1 << 32) }
        XCTAssertEqual(UInt64(AvatarLogic.hash("Weekend trip with the whole team")), expected)
    }

    func testColourIsStable() {
        XCTAssertEqual(AvatarLogic.colourHex(for: "A"), AvatarLogic.colours[65 % 8])
        XCTAssertEqual(AvatarLogic.colourHex(for: "Asha Rao"), AvatarLogic.colourHex(for: "Asha Rao"))
    }

    func testInitials() {
        XCTAssertEqual(AvatarLogic.initials("Asha Rao"), "AR")
        XCTAssertEqual(AvatarLogic.initials("Weekend trip"), "WT")
        XCTAssertEqual(AvatarLogic.initials("one two three"), "OT")
        XCTAssertEqual(AvatarLogic.initials("  madhu  "), "M")
        XCTAssertEqual(AvatarLogic.initials(""), "?")
    }

    func testFontSize() {
        XCTAssertEqual(AvatarLogic.fontSize(size: 100, group: false), 38, accuracy: 0.0001)
        XCTAssertEqual(AvatarLogic.fontSize(size: 100, group: true), 34, accuracy: 0.0001)
    }
}
