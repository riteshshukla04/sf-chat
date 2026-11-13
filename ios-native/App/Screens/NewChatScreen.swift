import SwiftUI
import ChatterCore

/// New chat modal (RN: src/app/new-chat.tsx): contacts by name; choosing one starts (or reuses)
/// the direct chat, closes the modal and opens that chat.
struct NewChatScreen: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.colors) private var c

    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 0) {
                Text("Contacts on Chatter").font(.system(size: 13, weight: .semibold)).foregroundColor(c.muted)
                    .padding(.horizontal, CGFloat(Spacing.lg)).padding(.top, CGFloat(Spacing.lg)).padding(.bottom, CGFloat(Spacing.sm))
                ForEach(sortedContacts(model.state.contacts), id: \.id) { contact in
                    Button {
                        let id = model.store.startChat(contact.id)
                        model.showingNewChat = false
                        model.openChat(id)
                    } label: {
                        HStack(spacing: CGFloat(Spacing.md)) {
                            AvatarView(name: contact.name, size: 44)
                            VStack(alignment: .leading) {
                                Text(contact.name).font(.system(size: 16, weight: .semibold)).foregroundColor(c.text)
                                Text(contact.about).font(.system(size: 14)).foregroundColor(c.muted).lineLimit(1)
                            }
                            Spacer(minLength: 0)
                        }
                        .padding(.horizontal, CGFloat(Spacing.lg)).padding(.vertical, CGFloat(Spacing.md))
                        .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                    .accessibilityIdentifier(TestID.contact(contact.id))
                }
            }
        }
        .background(c.surface)
        .navigationTitle("New chat")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Close") { model.showingNewChat = false } } }
        .accessibilityIdentifier(TestID.newChatScreen)
    }
}
