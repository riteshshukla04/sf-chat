import SwiftUI

/// State owned by the navigation shell.
@MainActor
final class ShellState: ObservableObject {
    /// Total unread messages for the Chats tab badge. Placeholder until the core task's `totalUnread`
    /// (src/lib/chats.ts) and the store land; then compute it from the stored chats here.
    @Published var unread: Int = 0

    /// A fatal error to show instead of the app (RN ErrorBoundary equivalent).
    @Published var error: String?

    func retry() { error = nil }
}

enum Route: Hashable {
    case chat(String)
}
