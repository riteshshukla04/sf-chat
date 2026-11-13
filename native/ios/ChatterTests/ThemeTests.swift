import XCTest
import UIKit
@testable import Chatter

final class ThemeTests: XCTestCase {
    private func resolved(_ pair: Theme.Pair, _ style: UIUserInterfaceStyle) -> UIColor {
        pair.uiColor.resolvedColor(with: UITraitCollection(userInterfaceStyle: style))
    }

    private func rgb(_ c: UIColor) -> [Int] {
        var r: CGFloat = 0, g: CGFloat = 0, b: CGFloat = 0, a: CGFloat = 0
        c.getRed(&r, green: &g, blue: &b, alpha: &a)
        return [r, g, b].map { Int(($0 * 255).rounded()) }
    }

    func testAccentLightAndDark() {
        XCTAssertEqual(rgb(resolved(Theme.accentPair, .light)), [0x12, 0x8C, 0x5E])
        XCTAssertEqual(rgb(resolved(Theme.accentPair, .dark)), [0x21, 0xC0, 0x7A])
    }

    func testChatBackgroundAndBubbles() {
        XCTAssertEqual(rgb(resolved(Theme.chatBackgroundPair, .light)), [0xEF, 0xEA, 0xE2])
        XCTAssertEqual(rgb(resolved(Theme.bubbleMinePair, .dark)), [0x0F, 0x5E, 0x44])
        XCTAssertEqual(rgb(resolved(Theme.dangerPair, .dark)), [0xFF, 0x6B, 0x66])
    }

    func testHexParsingInvalidIsBlack() {
        XCTAssertEqual(rgb(UIColor(hex: "nope")), [0, 0, 0])
    }

    func testSpacing() {
        XCTAssertEqual([Spacing.xs, Spacing.sm, Spacing.md, Spacing.lg, Spacing.xl], [4, 8, 12, 16, 24])
    }
}
