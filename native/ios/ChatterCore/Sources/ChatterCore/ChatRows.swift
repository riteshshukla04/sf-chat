import Foundation

/// One line of the conversation list: a day pill or a message. Port of `Row` in src/app/chat/[id].tsx.
public enum ChatRow: Equatable, Identifiable {
    case day(key: String, label: String)
    case message(Message)

    public var id: String {
        switch self {
        case .day(let key, _): return key
        case .message(let m): return m.id
        }
    }
}

/// Day pills and messages, oldest first (the screen scrolls to the bottom, so newest is last).
public func chatRows(_ messages: [Message], _ now: Int64, calendar: Calendar = .current) -> [ChatRow] {
    var out: [ChatRow] = []
    for group in groupByDay(messages, now, calendar: calendar) {
        out.append(.day(key: "day-\(group.day)", label: group.day))
        out.append(contentsOf: group.messages.map { .message($0) })
    }
    return out
}

/// Picker state: which message's reaction picker is open. Long-press toggles it.
public struct PickerState: Equatable {
    public private(set) var open: String?
    public init() {}
    public mutating func longPress(_ messageId: String) { open = open == messageId ? nil : messageId }
    public mutating func close() { open = nil }
}
