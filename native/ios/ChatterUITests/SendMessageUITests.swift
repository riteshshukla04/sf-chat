import XCTest

/// Mirrors .maestro/send-message.yaml.
final class SendMessageUITests: XCTestCase {
    func testSendMessage() {
        let app = XCUIApplication()
        app.launch()

        let row = app.descendants(matching: .any)["chat-row-asha"]
        XCTAssertTrue(row.waitForExistence(timeout: 10))
        row.tap()

        XCTAssertTrue(app.descendants(matching: .any)["chat-subtitle"].waitForExistence(timeout: 5))

        let input = app.descendants(matching: .any)["message-input"]
        input.tap()
        input.typeText("Looks good, approving now")
        app.buttons["send"].tap()

        XCTAssertTrue(app.staticTexts["Looks good, approving now"].waitForExistence(timeout: 5))
    }
}
