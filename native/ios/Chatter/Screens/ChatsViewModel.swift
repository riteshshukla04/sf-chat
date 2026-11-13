import ChatterCore
import Foundation

/// Display data for one row of the Chats list. Pure, so it is unit tested (ChatsViewModelTests).
struct ChatRowModel: Equatable, Identifiable {
    let id: String
    let title: String
    let isGroup: Bool
    let time: String
    /// ✓✓ read, 🕓 sending, ✓ otherwise; empty unless my message is the last one.
    let tickPrefix: String
    let preview: String
    let muted: Bool
    let pinned: Bool
    let unread: Int
    /// The time is drawn in the accent colour when there are unread messages and the chat isn't muted.
    let timeIsAccent: Bool
    /// The unread badge is grey when muted, accent otherwise.
    let badgeIsMuted: Bool

    /// What the row's subtitle shows: the tick prefix followed by the preview.
    var subtitle: String { tickPrefix + preview }
}

enum ChatsViewModel {
    /// Mirrors the tick prefix logic in src/app/(tabs)/index.tsx.
    static func tickPrefix(_ chat: Chat) -> String {
        guard let last = chat.messages.last, last.from == "me" else { return "" }
        switch last.status {
        case .read: return "✓✓ "
        case .sending: return "🕓 "
        default: return "✓ "
        }
    }

    static func row(_ chat: Chat, contacts: [String: Contact], now: Int64, calendar: Calendar = .current) -> ChatRowModel {
        ChatRowModel(
            id: chat.id,
            title: chat.title,
            isGroup: chat.kind == .group,
            time: formatTime(lastActivityAt(chat), now, calendar: calendar),
            tickPrefix: tickPrefix(chat),
            preview: previewText(chat, contacts),
            muted: chat.muted,
            pinned: chat.pinned,
            unread: chat.unread,
            timeIsAccent: chat.unread > 0 && !chat.muted,
            badgeIsMuted: chat.muted
        )
    }

    /// Filtered by the query (title or message text), pinned first, then latest activity.
    static func rows(_ chats: [Chat], contacts: [String: Contact], query: String, now: Int64, calendar: Calendar = .current) -> [ChatRowModel] {
        sortChats(searchChats(chats, query)).map { row($0, contacts: contacts, now: now, calendar: calendar) }
    }

    static func emptyText(query: String) -> String { "No chats match “\(query)”" }

    /// Contacts sorted by name, like `localeCompare` in src/app/new-chat.tsx.
    static func sortedContacts(_ contacts: [String: Contact]) -> [Contact] {
        contacts.values.sorted { $0.name.localizedCompare($1.name) == .orderedAscending }
    }
}
