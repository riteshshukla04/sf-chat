import SwiftUI
import ChatterCore

/// Chats list (RN: src/app/(tabs)/index.tsx): search, sorted rows, FAB opening the new-chat modal.
/// Tap opens the conversation, long-press toggles pin.
struct ChatsListScreen: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.colors) private var c
    @State private var query = ""

    var body: some View {
        let rows = chatRows(model.state.chats, contacts: model.state.contacts, query: query,
                            now: Millis((Date().timeIntervalSince1970 * 1000).rounded()))
        ZStack(alignment: .bottomTrailing) {
            VStack(spacing: 0) {
                Text("Chatter").font(.system(size: 26, weight: .bold)).foregroundColor(c.accent)
                    .accessibilityAddTraits(.isHeader)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, CGFloat(Spacing.lg)).padding(.vertical, CGFloat(Spacing.sm))
                search
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(rows, id: \.id) { row in
                            ChatRow(row: row)
                                .onTapGesture { model.openChat(row.id) }
                                .onLongPressGesture { model.store.togglePin(row.id) }
                                .accessibilityElement(children: .combine)
                                .accessibilityAddTraits(.isButton)
                                .accessibilityIdentifier(TestID.chatRow(row.id))
                        }
                        if rows.isEmpty {
                            Text(emptySearchText(query)).foregroundColor(c.muted)
                                .frame(maxWidth: .infinity).padding(.top, CGFloat(Spacing.xl))
                        }
                    }
                }
            }
            Button { model.showingNewChat = true } label: {
                Image(systemName: "square.and.pencil").font(.system(size: 24))
                    .foregroundColor(c.onAccent).frame(width: 58, height: 58)
                    .background(c.accent).clipShape(RoundedRectangle(cornerRadius: 18))
                    .shadow(radius: 4, y: 2)
            }
            .accessibilityLabel("New chat")
            .accessibilityIdentifier(TestID.newChat)
            .padding(CGFloat(Spacing.lg))
        }
        .background(c.surface)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestID.chatsScreen)
    }

    private var search: some View {
        HStack(spacing: CGFloat(Spacing.sm)) {
            Image(systemName: "magnifyingglass").foregroundColor(c.muted)
            TextField("", text: $query, prompt: Text("Search").foregroundColor(c.muted))
                .foregroundColor(c.text)
                .autocorrectionDisabled()
                .textInputAutocapitalization(.never)
                .accessibilityIdentifier("chat-search")
        }
        .padding(.horizontal, CGFloat(Spacing.md)).padding(.vertical, 10)
        .background(c.surfaceAlt).clipShape(RoundedRectangle(cornerRadius: 22))
        .padding(.horizontal, CGFloat(Spacing.lg)).padding(.bottom, CGFloat(Spacing.sm))
    }
}

private struct ChatRow: View {
    let row: ChatRowModel
    @Environment(\.colors) private var c

    var body: some View {
        HStack(spacing: CGFloat(Spacing.md)) {
            AvatarView(name: row.title, group: row.isGroup)
            VStack(spacing: 4) {
                HStack(spacing: CGFloat(Spacing.sm)) {
                    Text(row.title).font(.system(size: 17, weight: .semibold)).foregroundColor(c.text).lineLimit(1)
                    Spacer(minLength: 0)
                    Text(row.time).font(.system(size: 12)).foregroundColor(row.timeHighlighted ? c.accent : c.muted)
                }
                HStack(spacing: CGFloat(Spacing.sm)) {
                    Text(row.statusPrefix + row.preview).font(.system(size: 15)).foregroundColor(c.muted).lineLimit(1)
                    Spacer(minLength: 0)
                    HStack(spacing: 6) {
                        if row.muted { Image(systemName: "bell.slash").font(.system(size: 14)).foregroundColor(c.muted) }
                        if row.pinned { Image(systemName: "pin").font(.system(size: 14)).foregroundColor(c.muted) }
                        if row.unread > 0 {
                            Text("\(row.unread)").font(.system(size: 12, weight: .bold)).foregroundColor(c.onAccent)
                                .padding(.horizontal, 6).frame(minWidth: 22, minHeight: 22)
                                .background(row.muted ? c.muted : c.accent).clipShape(Capsule())
                        }
                    }
                }
            }
        }
        .padding(.horizontal, CGFloat(Spacing.lg)).padding(.vertical, CGFloat(Spacing.md))
        .background(c.surface)
        .contentShape(Rectangle())
    }
}
