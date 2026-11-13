import SwiftUI
import ChatterCore

/// PLACEHOLDER: the new-chat task replaces this file (RN: src/app/new-chat.tsx), shown as a modal.
/// Until then: contacts list; choosing one starts (or reuses) the direct chat and opens it.
struct NewChatScreen: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.colors) private var c

    var body: some View {
        List(model.state.contacts.values.sorted { $0.name < $1.name }, id: \.id) { contact in
            Button {
                let id = model.store.startChat(contact.id)
                model.showingNewChat = false
                model.openChat(id)
            } label: { Text(contact.name).foregroundColor(c.text) }
            .accessibilityIdentifier(TestID.contact(contact.id))
        }
        .listStyle(.plain)
        .navigationTitle("New chat")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Close") { model.showingNewChat = false } } }
        .accessibilityIdentifier(TestID.newChatScreen)
    }
}
