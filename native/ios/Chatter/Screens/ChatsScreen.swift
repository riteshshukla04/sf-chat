import SwiftUI

/// Stub. Chats task replaces this file. Must expose: chats-screen, chat-search, chat-row-<id>,
/// chat-subtitle, new-chat (calling `newChat`) and call `openChat(id)` on row tap.
struct ChatsScreen: View {
    let openChat: (String) -> Void
    let newChat: () -> Void

    var body: some View {
        VStack(spacing: Spacing.lg) {
            Text("Chats").font(.title2.bold()).foregroundColor(Theme.text)
            Button("New chat", action: newChat).accessibilityIdentifier("new-chat")
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Theme.background)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("chats-screen")
    }
}
