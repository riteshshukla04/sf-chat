import SwiftUI

/// Stub. Chat task replaces this file (identifiers: chat-screen, message-input, send, message-<id>, ...).
/// The navigation title is blank; this screen supplies the header (avatar + title + subtitle) through
/// the principal toolbar slot below.
struct ChatScreen: View {
    let chatId: String

    var body: some View {
        Text("Chat \(chatId)").foregroundColor(Theme.text)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Theme.chatBackground)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    HStack(spacing: Spacing.sm) {
                        AvatarView(name: chatId, size: 32)
                        VStack(alignment: .leading, spacing: 0) {
                            Text(chatId).font(.headline).foregroundColor(Theme.text)
                            Text("").font(.caption).foregroundColor(Theme.muted)
                                .accessibilityIdentifier("chat-subtitle")
                        }
                    }
                }
            }
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("chat-screen")
    }
}
