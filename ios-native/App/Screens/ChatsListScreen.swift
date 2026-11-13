import SwiftUI
import ChatterCore

/// PLACEHOLDER: the chats-list task replaces this file (RN: src/app/(tabs)/index.tsx).
/// Until then: a plain list of chats (pushes route chat/<id>) and the new-chat button.
struct ChatsListScreen: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.colors) private var c

    var body: some View {
        VStack(spacing: 0) {
            Text("Chatter").font(.title.bold()).foregroundColor(c.text)
                .frame(maxWidth: .infinity, alignment: .leading).padding(CGFloat(Spacing.lg))
            List(sortChats(model.state.chats), id: \.id) { chat in
                Button { model.openChat(chat.id) } label: {
                    VStack(alignment: .leading) {
                        Text(chat.title).foregroundColor(c.text)
                        Text(previewText(chat, contacts: model.state.contacts)).font(.footnote).foregroundColor(c.muted)
                    }
                }
                .accessibilityIdentifier(TestID.chatRow(chat.id))
            }
            .listStyle(.plain)
            Button { model.showingNewChat = true } label: {
                Image(systemName: "plus").foregroundColor(c.onAccent).frame(width: 56, height: 56)
                    .background(c.accent).clipShape(Circle())
            }
            .accessibilityLabel("New chat")
            .accessibilityIdentifier(TestID.newChat)
            .padding()
        }
        .background(c.surface)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestID.chatsScreen)
    }
}
