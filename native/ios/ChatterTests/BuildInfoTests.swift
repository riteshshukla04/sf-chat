import XCTest
@testable import Chatter

final class BuildInfoTests: XCTestCase {
    func testBuildInfo() {
        let info = BuildInfo.current
        XCTAssertEqual(info.appVersion, "1.0.0")
        XCTAssertEqual(info.channel, "chat")
        XCTAssertEqual(info.updateLabel, "embedded")
        XCTAssertEqual(info.release, "dev.softwarefactory.chatter@1.0.0")
        XCTAssertEqual(info.dist, "embedded")
    }

    func testSentryDsnParsing() {
        XCTAssertNil(SentryService.dsn(from: nil))
        XCTAssertNil(SentryService.dsn(from: ["SentryDSN": ""]))
        XCTAssertNil(SentryService.dsn(from: ["SentryDSN": "  "]))
        XCTAssertNil(SentryService.dsn(from: ["SentryDSN": "$(SENTRY_DSN)"]))
        XCTAssertEqual(SentryService.dsn(from: ["SentryDSN": "https://k@o1.ingest.sentry.io/2"]), "https://k@o1.ingest.sentry.io/2")
    }
}
