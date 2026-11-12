import ChatterCore
import SwiftUI

/// Port of src/app/new-chat.tsx. The sheet title "New chat" is set by the shell.
/// `openChat` (from the shell) dismisses the sheet and pushes the chat.
struct NewChatScreen: View {
    let openChat: (String) -> Void
    let dismiss: () -> Void

    @Environment(AppStore.self) private var store

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                Text("Contacts on Chatter")
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(Theme.muted)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, Spacing.lg)
                    .padding(.top, Spacing.lg)
                    .padding(.bottom, Spacing.sm)
                ForEach(ChatsViewModel.sortedContacts(store.contacts)) { contact in
                    HStack(spacing: Spacing.md) {
                        AvatarView(name: contact.name, size: 44)
                        VStack(alignment: .leading, spacing: 0) {
                            Text(contact.name).font(.system(size: 16, weight: .semibold)).foregroundColor(Theme.text)
                            Text(contact.about).font(.system(size: 14)).foregroundColor(Theme.muted).lineLimit(1)
                        }
                        Spacer(minLength: 0)
                    }
                    .padding(.horizontal, Spacing.lg)
                    .padding(.vertical, Spacing.md)
                    .contentShape(Rectangle())
                    .onTapGesture { openChat(store.startChat(contact.id)) }
                    .accessibilityElement(children: .contain)
                    .accessibilityAddTraits(.isButton)
                    .accessibilityIdentifier("contact-\(contact.id)")
                }
            }
        }
        .background(Theme.surface)
        .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Close", action: dismiss) } }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("new-chat-screen")
    }
}
