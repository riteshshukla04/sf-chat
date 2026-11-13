import XCTest
@testable import ChatterCore

private let NOW: Millis = 1_794_000_000_000
private let contacts = ["asha": Contact(id: "asha", name: "Asha Rao", about: "", lastSeenAt: nil)]
private func info(label: String = "embedded") -> BuildInfo {
    BuildInfo(appVersion: "1.2.3", runtimeVersion: "1.2.3", channel: "none", updateId: nil, updateLabel: label,
              updatesEnabled: false, platform: "ios", release: "x@1.2.3", dist: label)
}
private struct Boom: Error {}
private struct Described: LocalizedError { var errorDescription: String? { "bad thing" } }

final class CallsSettingsPresentationTests: XCTestCase {
    func testCallRowKnownContactAndDirections() {
        let out = callRowModel(Call(id: "c1", contactId: "asha", at: NOW - 1000, direction: .outgoing, video: false), contacts: contacts, now: NOW)
        XCTAssertEqual(out.name, "Asha Rao")
        XCTAssertFalse(out.missed)
        XCTAssertEqual(out.directionSymbol, "phone.arrow.up.right")
        XCTAssertEqual(out.kindSymbol, "phone")
        let inc = callRowModel(Call(id: "c2", contactId: "asha", at: NOW, direction: .incoming, video: true), contacts: contacts, now: NOW)
        XCTAssertEqual(inc.directionSymbol, "phone.arrow.down.left")
        XCTAssertEqual(inc.kindSymbol, "video")
    }

    func testMissedCallIsDangerAndUnknownContact() {
        let m = callRowModel(Call(id: "c3", contactId: "ghost", at: NOW, direction: .missed, video: false), contacts: contacts, now: NOW)
        XCTAssertTrue(m.missed)
        XCTAssertEqual(m.name, "Unknown")
        XCTAssertEqual(m.directionSymbol, "phone.down")
    }

    func testCallTimeUsesFormatTimeAndOrderIsKept() {
        let cal = Calendar.current
        let calls = [Call(id: "a", contactId: "asha", at: NOW - 3 * 86_400_000 - 1, direction: .incoming, video: false),
                     Call(id: "b", contactId: "asha", at: NOW, direction: .incoming, video: false)]
        let rows = callRows(calls, contacts: contacts, now: NOW, calendar: cal)
        XCTAssertEqual(rows.map(\.id), ["a", "b"])
        XCTAssertEqual(rows[0].time, formatTime(calls[0].at, now: NOW, calendar: cal))
    }

    func testVersionText() {
        XCTAssertEqual(versionText(info()), "1.2.3 (built in)")
        XCTAssertEqual(versionText(info(label: "0123456789abcdef")), "1.2.3 (01234567)")
        XCTAssertEqual(versionText(info(label: "abc")), "1.2.3 (abc)")
    }

    func testSettingsRowsMatchRN() {
        let rows = settingsRows(info: info(), crashReportingEnabled: false)
        XCTAssertEqual(rows.map(\.label), ["Account", "Notifications", "Storage", "Version", "Update channel", "Crash reporting"])
        XCTAssertEqual(rows[2].value, "Messages are kept on this phone")
        XCTAssertEqual(rows[3].testID, "version-row")
        XCTAssertEqual(rows[4].value, "none")
        XCTAssertEqual(rows[5].value, "off (no DSN)")
        XCTAssertEqual(rows[5].testID, "crash-reporting-row")
        XCTAssertEqual(settingsRows(info: info(), crashReportingEnabled: true)[5].value, "Sentry on")
    }

    func testBuildInfoFacts() {
        let b = getBuildInfo(bundle: Bundle(for: BundleToken.self))
        XCTAssertEqual(b.updateLabel, "embedded")
        XCTAssertEqual(b.platform, "ios")
        XCTAssertEqual(b.dist, "embedded")
        XCTAssertEqual(b.runtimeVersion, b.appVersion)
        XCTAssertNil(b.updateId)
    }

    func testProfileDraft() {
        XCTAssertEqual(ProfileDraft(name: "  Ana ", about: " hi\n").trimmed, ProfileDraft(name: "Ana", about: "hi"))
        XCTAssertFalse(ProfileDraft(name: "   ", about: "x").isValid)
        XCTAssertTrue(ProfileDraft(name: "A", about: "").isValid)
        XCTAssertEqual(ProfileDraft(Profile(name: "N", about: "A")), ProfileDraft(name: "N", about: "A"))
    }

    func testStoreProfileEditAndReset() {
        let store = ChatStore(storage: InMemoryStorage())
        store.hydrate(now: NOW)
        let seedCalls = store.state.calls
        store.updateProfile(name: "Ana", about: "Busy")
        XCTAssertEqual(store.state.profile, Profile(name: "Ana", about: "Busy"))
        store.markRead(store.state.chats[0].id)
        store.resetChats(now: NOW)
        XCTAssertEqual(store.state.calls, seedCalls)
        XCTAssertEqual(store.state.profile.name, "Ana") // reset keeps the profile, like RN
    }

    func testErrorTexts() {
        XCTAssertEqual(ErrorScreenText.title, "Something went wrong")
        XCTAssertEqual(ErrorScreenText.retry, "Try again")
        XCTAssertEqual(errorMessageText(Described()), "bad thing")
        XCTAssertFalse(errorMessageText(Boom()).isEmpty)
    }

    func testErrorIsReported() {
        final class Spy: CrashReporter {
            var enabled: Bool { true }
            var seen: [String] = []
            func captureException(_ error: Error) { seen.append(errorMessageText(error)) }
            func addBreadcrumb(_ message: String, data: [String: String]?) {}
        }
        let spy = Spy()
        spy.captureException(Described())
        XCTAssertEqual(spy.seen, ["bad thing"])
    }
}
private final class BundleToken {}
