import SwiftUI
import ChatterCore

extension Color {
    /// "#RRGGBB" → Color.
    init(hex: String) {
        let v = UInt64(hex.dropFirst(), radix: 16) ?? 0
        self.init(.sRGB, red: Double((v >> 16) & 0xFF) / 255, green: Double((v >> 8) & 0xFF) / 255, blue: Double(v & 0xFF) / 255, opacity: 1)
    }
}

/// SwiftUI colours for one palette (RN: the `Colors` returned by useColors()).
struct ThemeColors {
    let background, surface, surfaceAlt, chatBackground, text, muted, border: Color
    let accent, accentStrong, onAccent, bubbleMine, bubbleTheirs, tickRead, danger: Color

    init(_ c: Colors) {
        background = Color(hex: c.background); surface = Color(hex: c.surface); surfaceAlt = Color(hex: c.surfaceAlt)
        chatBackground = Color(hex: c.chatBackground); text = Color(hex: c.text); muted = Color(hex: c.muted)
        border = Color(hex: c.border); accent = Color(hex: c.accent); accentStrong = Color(hex: c.accentStrong)
        onAccent = Color(hex: c.onAccent); bubbleMine = Color(hex: c.bubbleMine); bubbleTheirs = Color(hex: c.bubbleTheirs)
        tickRead = Color(hex: c.tickRead); danger = Color(hex: c.danger)
    }

    static let light = ThemeColors(Palette.light)
    static let dark = ThemeColors(Palette.dark)
}

private struct ColorsKey: EnvironmentKey { static let defaultValue = ThemeColors.light }

extension EnvironmentValues {
    var colors: ThemeColors {
        get { self[ColorsKey.self] }
        set { self[ColorsKey.self] = newValue }
    }
}

/// Injects the palette that matches the system light/dark setting.
struct ThemedRoot<Content: View>: View {
    @Environment(\.colorScheme) private var scheme
    let content: () -> Content
    var body: some View {
        let colors = scheme == .dark ? ThemeColors.dark : ThemeColors.light
        content()
            .environment(\.colors, colors)
            .tint(colors.accent)
            .background(colors.background.ignoresSafeArea())
    }
}
