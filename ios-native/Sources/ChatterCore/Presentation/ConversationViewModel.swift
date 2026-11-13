import Foundation

/// Presentation logic for the conversation screen (RN: the row building and bubble rendering in src/app/chat/[id].tsx).
public enum ConversationRow: Equatable {
    case day(key: String, label: String)
    case message(key: String, MessageBubble)

    public var key: String {
        switch self {
        case .day(let key, _), .message(let key, _): return key
        }
    }
}

public enum TickIcon: String, Equatable {
    case clock, check, checkAll
}

public struct MessageBubble: Equatable {
    public let id: String
    public let text: String
    public let mine: Bool
    public let clock: String
    /// Sender name shown above theirs-bubbles in groups; nil otherwise.
    public let senderName: String?
    /// Tick for my messages only.
    public let tick: TickIcon?
    public let tickIsRead: Bool
    /// "ticks-<status>", the RN testID of the tick icon.
    public let tickTestID: String?
    public var testID: String { "message-\(id)" }
}

public enum ConversationViewModel {
    public static let missingChatText = "This chat doesn't exist any more."
    public static let emptyNotice = "Messages are kept on this phone. Say hi 👋"

    /// Day headers and messages, oldest first (the view shows them top to bottom, newest at the bottom).
    public static func rows(chat: Chat, contacts: [String: Contact], now: Millis, calendar: Calendar = .current) -> [ConversationRow] {
        var out: [ConversationRow] = []
        for group in groupByDay(chat.messages, now: now, calendar: calendar) {
            out.append(.day(key: "day-\(group.day)", label: group.day))
            for m in group.messages {
                out.append(.message(key: m.id, bubble(m, chat: chat, contacts: contacts, calendar: calendar)))
            }
        }
        return out
    }

    public static func bubble(_ m: Message, chat: Chat, contacts: [String: Contact], calendar: Calendar = .current) -> MessageBubble {
        let mine = m.from == "me"
        let sender: String? = chat.kind == .group && !mine ? firstName(contacts[m.from]?.name ?? "Someone") : nil
        return MessageBubble(
            id: m.id, text: m.text, mine: mine, clock: formatClock(m.sentAt, calendar: calendar), senderName: sender,
            tick: mine ? tick(m.status) : nil, tickIsRead: mine && m.status == .read,
            tickTestID: mine ? "ticks-\(m.status.rawValue)" : nil)
    }

    public static func tick(_ status: MessageStatus) -> TickIcon {
        switch status {
        case .sending: return .clock
        case .sent: return .check
        case .delivered, .read: return .checkAll
        }
    }

    /// Header subtitle; empty groups have no last message, so they get just the members.
    public static func subtitle(chat: Chat, contacts: [String: Contact], now: Millis, calendar: Calendar = .current) -> String {
        if chat.kind == .group && chat.messages.isEmpty {
            let names = chat.members.map { firstName(contacts[$0]?.name ?? "Someone") }
            return "\(names.joined(separator: ", ")), You"
        }
        return headerSubtitle(chat, contacts: contacts, now: now, calendar: calendar)
    }

    public static func showsEmptyNotice(_ chat: Chat) -> Bool { chat.messages.isEmpty }
}
