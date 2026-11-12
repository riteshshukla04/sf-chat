import SwiftUI
import UIKit

/// Mirrors `src/constants/theme.ts` exactly. Each colour is dynamic: light value in light mode,
/// dark value in dark mode.
enum Theme {
    struct Pair {
        let light: String
        let dark: String

        var uiColor: UIColor {
            let l = UIColor(hex: light), d = UIColor(hex: dark)
            return UIColor { $0.userInterfaceStyle == .dark ? d : l }
        }
        var color: Color { Color(uiColor: uiColor) }
    }

    // name: (light, dark) — same keys and values as Palette in theme.ts.
    static let backgroundPair = Pair(light: "#FFFFFF", dark: "#0B141A")
    static let surfacePair = Pair(light: "#FFFFFF", dark: "#111B21")
    static let surfaceAltPair = Pair(light: "#F2F4F3", dark: "#1F2C34")
    static let chatBackgroundPair = Pair(light: "#EFEAE2", dark: "#0B141A")
    static let textPair = Pair(light: "#111B21", dark: "#E9EDEF")
    static let mutedPair = Pair(light: "#667781", dark: "#8696A0")
    static let borderPair = Pair(light: "#E4E8E6", dark: "#22303A")
    static let accentPair = Pair(light: "#128C5E", dark: "#21C07A")
    static let accentStrongPair = Pair(light: "#0B6E4A", dark: "#128C5E")
    static let onAccentPair = Pair(light: "#FFFFFF", dark: "#0B141A")
    static let bubbleMinePair = Pair(light: "#D7F5DE", dark: "#0F5E44")
    static let bubbleTheirsPair = Pair(light: "#FFFFFF", dark: "#1F2C34")
    static let tickReadPair = Pair(light: "#2E9BF0", dark: "#53BDEB")
    static let dangerPair = Pair(light: "#C9302C", dark: "#FF6B66")

    static var background: Color { backgroundPair.color }
    static var surface: Color { surfacePair.color }
    static var surfaceAlt: Color { surfaceAltPair.color }
    static var chatBackground: Color { chatBackgroundPair.color }
    static var text: Color { textPair.color }
    static var muted: Color { mutedPair.color }
    static var border: Color { borderPair.color }
    static var accent: Color { accentPair.color }
    static var accentStrong: Color { accentStrongPair.color }
    static var onAccent: Color { onAccentPair.color }
    static var bubbleMine: Color { bubbleMinePair.color }
    static var bubbleTheirs: Color { bubbleTheirsPair.color }
    static var tickRead: Color { tickReadPair.color }
    static var danger: Color { dangerPair.color }
}

enum Spacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 12
    static let lg: CGFloat = 16
    static let xl: CGFloat = 24
}

extension UIColor {
    /// Parses "#RRGGBB" (the leading # is optional). Invalid input yields black.
    convenience init(hex: String) {
        let (r, g, b) = Self.rgb(hex: hex)
        self.init(red: r, green: g, blue: b, alpha: 1)
    }

    static func rgb(hex: String) -> (CGFloat, CGFloat, CGFloat) {
        let s = hex.hasPrefix("#") ? String(hex.dropFirst()) : hex
        guard s.count == 6, let v = UInt32(s, radix: 16) else { return (0, 0, 0) }
        return (CGFloat((v >> 16) & 0xFF) / 255, CGFloat((v >> 8) & 0xFF) / 255, CGFloat(v & 0xFF) / 255)
    }
}
