import Foundation

// Presentation logic for the Chats list, New chat and Avatar (RN: src/app/(tabs)/index.tsx,
// src/app/new-chat.tsx, src/components/avatar.tsx). Kept free of UI so it is unit-testable.

/// Avatar background palette (RN: COLOURS in avatar.tsx).
public let avatarColours = ["#25A36F", "#3E8EDE", "#D9822B", "#9B59B6", "#E0566E", "#16A2B8", "#7A8B2E", "#C0612B"]

/// A stable colour per name (RN: colourFor). Same hash: h = (h*31 + charCodeAt(0)) >>> 0,
/// iterating code points and using the first UTF-16 unit of each.
public func avatarColour(_ name: String) -> String {
    var hash: UInt32 = 0
    for scalar in name.unicodeScalars {
        let unit = String(scalar).utf16.first!
        hash = hash &* 31 &+ UInt32(unit)
    }
    return avatarColours[Int(hash) % avatarColours.count]
}

/// Font scale of the initials relative to the avatar size.
public func avatarFontScale(group: Bool) -> Double { group ? 0.34 : 0.38 }

/// Everything one row of the chats list shows, already worked out.
public struct ChatRowModel: Equatable {
    public var id: String
    public var title: String
    public var isGroup: Bool
    public var time: String
    /// Highlight the time in the accent colour (unread and not muted).
    public var timeHighlighted: Bool
    /// "✓✓ " (read), "🕓 " (sending), "✓ " (other) for my last message, else "".
    public var statusPrefix: String
    public var preview: String
    public var muted: Bool
    public var pinned: Bool
    public var unread: Int
}

public func chatRowModel(_ chat: Chat, contacts: [String: Contact], now: Millis, calendar: Calendar = .current) -> ChatRowModel {
    var prefix = ""
    if let last = chat.messages.last, last.from == "me" {
        prefix = last.status == .read ? "✓✓ " : last.status == .sending ? "🕓 " : "✓ "
    }
    return ChatRowModel(
        id: chat.id, title: chat.title, isGroup: chat.kind == .group,
        time: formatTime(lastActivityAt(chat), now: now, calendar: calendar),
        timeHighlighted: chat.unread > 0 && !chat.muted,
        statusPrefix: prefix,
        preview: previewText(chat, contacts: contacts),
        muted: chat.muted, pinned: chat.pinned, unread: chat.unread)
}

/// The rows shown for a search query: filtered, then pinned/recent first.
public func chatRows(_ chats: [Chat], contacts: [String: Contact], query: String, now: Millis,
                     calendar: Calendar = .current) -> [ChatRowModel] {
    sortChats(searchChats(chats, query: query)).map { chatRowModel($0, contacts: contacts, now: now, calendar: calendar) }
}

/// Text when no chat matches (RN: ListEmptyComponent).
public func emptySearchText(_ query: String) -> String { "No chats match “\(query)”" }

/// Contacts for the New chat modal, ordered by name (RN: localeCompare).
public func sortedContacts(_ contacts: [String: Contact]) -> [Contact] {
    contacts.values.sorted { a, b in
        let r = a.name.localizedCompare(b.name)
        return r == .orderedSame ? a.id < b.id : r == .orderedAscending
    }
}
