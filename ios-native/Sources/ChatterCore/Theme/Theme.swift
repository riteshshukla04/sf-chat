// Port of src/constants/theme.ts. Colours are kept as the same hex strings so they can be compared
// exactly; the SwiftUI app turns them into `Color` (App/Theme/Theme+SwiftUI.swift).

public struct Colors: Equatable {
    public let background, surface, surfaceAlt, chatBackground, text, muted, border: String
    public let accent, accentStrong, onAccent, bubbleMine, bubbleTheirs, tickRead, danger: String
}

public enum Palette {
    public static let light = Colors(
        background: "#FFFFFF", surface: "#FFFFFF", surfaceAlt: "#F2F4F3", chatBackground: "#EFEAE2",
        text: "#111B21", muted: "#667781", border: "#E4E8E6", accent: "#1A73E8", accentStrong: "#1557B0",
        onAccent: "#FFFFFF", bubbleMine: "#D7F5DE", bubbleTheirs: "#FFFFFF", tickRead: "#2E9BF0", danger: "#C9302C")
    public static let dark = Colors(
        background: "#0B141A", surface: "#111B21", surfaceAlt: "#1F2C34", chatBackground: "#0B141A",
        text: "#E9EDEF", muted: "#8696A0", border: "#22303A", accent: "#4C9AFF", accentStrong: "#1A73E8",
        onAccent: "#0B141A", bubbleMine: "#0F5E44", bubbleTheirs: "#1F2C34", tickRead: "#53BDEB", danger: "#FF6B66")
}

public enum Spacing {
    public static let xs: Double = 4, sm: Double = 8, md: Double = 12, lg: Double = 16, xl: Double = 24
}

/// Stable ids the RN app exposes as testID (and the Maestro flows use); used as accessibility identifiers.
public enum TestID {
    public static func chatRow(_ id: String) -> String { "chat-row-\(id)" }
    public static let chatSubtitle = "chat-subtitle"
    public static let messageInput = "message-input"
    public static let send = "send"
    public static let newChat = "new-chat"
    public static let tabChats = "tab-chats"
    public static let tabCalls = "tab-calls"
    public static let tabSettings = "tab-settings"
    public static let errorScreen = "error-screen"
    public static let errorMessage = "error-message"
    public static let errorRetry = "error-retry"
    // Screen roots, as in the RN screens.
    public static let chatsScreen = "chats-screen"
    public static let chatScreen = "chat-screen"
    public static let newChatScreen = "new-chat-screen"
    public static let callsScreen = "calls-screen"
    public static let settingsScreen = "settings-screen"
    public static func contact(_ id: String) -> String { "contact-\(id)" }
}
