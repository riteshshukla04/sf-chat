import SwiftUI

// A stable colour per name, so the same person always gets the same avatar.
// Mirrors src/components/avatar.tsx.
enum AvatarLogic {
    static let colours = ["#25A36F", "#3E8EDE", "#D9822B", "#9B59B6", "#E0566E", "#16A2B8", "#7A8B2E", "#C0612B"]

    /// hash = (hash * 31 + code) mod 2^32 per code point. Like the RN app's
    /// `for (const ch of name) ... ch.charCodeAt(0)`, this uses the first UTF-16 code unit of each
    /// code point (the high surrogate for characters outside the BMP).
    static func hash(_ name: String) -> UInt32 {
        var h: UInt32 = 0
        for scalar in name.unicodeScalars {
            let unit = UInt32(scalar.utf16.first!)
            h = h &* 31 &+ unit
        }
        return h
    }

    static func colourHex(for name: String) -> String {
        colours[Int(hash(name) % UInt32(colours.count))]
    }

    /// "Asha Rao" → "AR", "Weekend trip" → "WT". Same rules as `initials` in src/lib/chats.ts.
    /// (Move to the chats core file once it lands.)
    static func initials(_ name: String) -> String {
        let letters = name
            .components(separatedBy: .whitespacesAndNewlines)
            .filter { !$0.isEmpty }
            .prefix(2)
            .compactMap { $0.first.map { String($0).uppercased() } }
            .joined()
        return letters.isEmpty ? "?" : letters
    }

    static func fontSize(size: CGFloat, group: Bool) -> CGFloat {
        size * (group ? 0.34 : 0.38)
    }
}

struct AvatarView: View {
    let name: String
    var size: CGFloat = 48
    var group: Bool = false

    var body: some View {
        Text(AvatarLogic.initials(name))
            .font(.system(size: AvatarLogic.fontSize(size: size, group: group), weight: .bold))
            .foregroundColor(.white)
            .frame(width: size, height: size)
            .background(Circle().fill(Color(uiColor: UIColor(hex: AvatarLogic.colourHex(for: name)))))
            .accessibilityHidden(true)
    }
}
