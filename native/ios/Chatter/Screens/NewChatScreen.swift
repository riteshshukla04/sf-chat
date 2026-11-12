import SwiftUI

/// Stub. New-chat task replaces this file (identifiers: new-chat-screen, contact-<id>). The sheet
/// title "New chat" is set by the shell. Call `openChat(id)` after creating/selecting a chat.
struct NewChatScreen: View {
    let openChat: (String) -> Void
    let dismiss: () -> Void

    var body: some View {
        Text("New chat").foregroundColor(Theme.text)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Theme.background)
            .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Close", action: dismiss) } }
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("new-chat-screen")
    }
}
