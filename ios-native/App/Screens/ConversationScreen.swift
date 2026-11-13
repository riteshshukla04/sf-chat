import SwiftUI
import ChatterCore

/// PLACEHOLDER: the conversation task replaces this file (RN: src/app/chat/[id].tsx).
/// Route chat/<id>; shows the header title and `chat-subtitle` only (no messages, input or send yet).
struct ConversationScreen: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.colors) private var c
    let chatId: String

    var body: some View {
        VStack(spacing: CGFloat(Spacing.sm)) {
            if let chat = model.chat(id: chatId) {
                Text(chat.title).font(.headline).foregroundColor(c.text)
                Text(headerSubtitle(chat, contacts: model.state.contacts, now: Millis(Date().timeIntervalSince1970 * 1000)))
                    .font(.footnote).foregroundColor(c.muted)
                    .accessibilityIdentifier(TestID.chatSubtitle)
            } else {
                Text("Chat not found").foregroundColor(c.muted)
            }
            Text("Conversation placeholder").foregroundColor(c.muted)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(c.chatBackground)
        .navigationTitle("")
        .navigationBarTitleDisplayMode(.inline)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestID.chatScreen)
        .onAppear { model.store.markRead(chatId) }
    }
}

