import ChatterCore
import XCTest
@testable import Chatter

final class ScreenLabelTests: XCTestCase {
    func testVersionValue() {
        XCTAssertEqual(SettingsLogic.versionValue(BuildInfo()), "1.0.0 (built in)")
        var ota = BuildInfo(); ota.updateLabel = "abcdef0123456789"
        XCTAssertEqual(SettingsLogic.versionValue(ota), "1.0.0 (abcdef01)")
    }

    func testCrashReportingValue() {
        XCTAssertEqual(SettingsLogic.crashReportingValue(enabled: false), "off (no DSN)")
        XCTAssertEqual(SettingsLogic.crashReportingValue(enabled: true), "Sentry on")
    }

    func testCallName() {
        let contacts = ["a": Contact(id: "a", name: "Asha Rao", about: "", lastSeenAt: nil)]
        let known = Call(id: "1", contactId: "a", at: 0, direction: .incoming, video: false)
        let gone = Call(id: "2", contactId: "zzz", at: 0, direction: .missed, video: true)
        XCTAssertEqual(CallsLogic.name(for: known, contacts: contacts), "Asha Rao")
        XCTAssertEqual(CallsLogic.name(for: gone, contacts: contacts), "Unknown")
        XCTAssertFalse(CallsLogic.isMissed(known))
        XCTAssertTrue(CallsLogic.isMissed(gone))
    }

    func testIcons() {
        let video = Call(id: "1", contactId: "a", at: 0, direction: .outgoing, video: true)
        let voice = Call(id: "2", contactId: "a", at: 0, direction: .outgoing, video: false)
        XCTAssertEqual(CallsLogic.trailingSymbol(video), "video")
        XCTAssertEqual(CallsLogic.trailingSymbol(voice), "phone")
        XCTAssertEqual(CallsLogic.directionLabel(.missed), "Missed")
        XCTAssertNotEqual(CallsLogic.directionSymbol(.outgoing), CallsLogic.directionSymbol(.incoming))
    }

    func testTimeLabelUsesFormatTime() {
        let now: Int64 = 1_700_000_000_000
        let call = Call(id: "1", contactId: "a", at: now - 2 * 24 * 3_600_000 - 1, direction: .incoming, video: false)
        XCTAssertEqual(CallsLogic.timeLabel(call, now: now), formatTime(call.at, now))
    }
}
