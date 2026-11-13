import ChatterCore
import SwiftUI

/// Port of src/app/(tabs)/index.tsx.
struct ChatsScreen: View {
    let openChat: (String) -> Void
    let newChat: () -> Void

    @Environment(AppStore.self) private var store
    @State private var query = ""

    var body: some View {
        // Relative times ("14:05", "Yesterday") refresh every 60 s, like useNow in the RN app.
        TimelineView(.periodic(from: .now, by: 60)) { context in
            content(now: Int64(context.date.timeIntervalSince1970 * 1000))
        }
        .background(Theme.surface)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("chats-screen")
    }

    private func content(now: Int64) -> some View {
        let rows = ChatsViewModel.rows(store.chats, contacts: store.contacts, query: query, now: now)
        return ZStack(alignment: .bottomTrailing) {
            VStack(spacing: 0) {
                header
                search
                ScrollView {
                    LazyVStack(spacing: 0) {
                        if rows.isEmpty {
                            Text(ChatsViewModel.emptyText(query: query))
                                .foregroundColor(Theme.muted)
                                .multilineTextAlignment(.center)
                                .frame(maxWidth: .infinity)
                                .padding(.top, Spacing.xl)
                        }
                        ForEach(rows) { row in
                            ChatRow(model: row)
                                .contentShape(Rectangle())
                                .onTapGesture { openChat(row.id) }
                                .onLongPressGesture { store.togglePin(row.id) }
                                .accessibilityElement(children: .contain)
                                .accessibilityIdentifier("chat-row-\(row.id)")
                                .accessibilityAddTraits(.isButton)
                        }
                    }
                }
            }
            Button(action: newChat) {
                Image(systemName: "square.and.pencil")
                    .font(.system(size: 24))
                    .foregroundColor(Theme.onAccent)
                    .frame(width: 58, height: 58)
                    .background(RoundedRectangle(cornerRadius: 18).fill(Theme.accent))
            }
            .padding(Spacing.lg)
            .accessibilityLabel("New chat")
            .accessibilityIdentifier("new-chat")
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text("Chatter")
                .font(.system(size: 26, weight: .bold))
                .foregroundColor(Theme.accent)
                .accessibilityAddTraits(.isHeader)
            if let note = BuildInfo.releaseNote {
                Text(note)
                    .font(.system(size: 13))
                    .foregroundColor(Theme.muted)
                    .accessibilityIdentifier("release-note")
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, Spacing.lg)
        .padding(.top, Spacing.sm)
        .padding(.bottom, Spacing.sm)
    }

    private var search: some View {
        HStack(spacing: Spacing.sm) {
            Image(systemName: "magnifyingglass").foregroundColor(Theme.muted)
            TextField("", text: $query, prompt: Text("Search").foregroundColor(Theme.muted))
                .foregroundColor(Theme.text)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .padding(.vertical, 10)
                .accessibilityIdentifier("chat-search")
        }
        .padding(.horizontal, Spacing.md)
        .background(RoundedRectangle(cornerRadius: 22).fill(Theme.surfaceAlt))
        .padding(.horizontal, Spacing.lg)
        .padding(.bottom, Spacing.sm)
    }
}

private struct ChatRow: View {
    let model: ChatRowModel

    var body: some View {
        HStack(spacing: Spacing.md) {
            AvatarView(name: model.title, group: model.isGroup)
            VStack(spacing: 4) {
                HStack(spacing: Spacing.sm) {
                    Text(model.title)
                        .font(.system(size: 17, weight: .semibold))
                        .foregroundColor(Theme.text)
                        .lineLimit(1)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    Text(model.time)
                        .font(.system(size: 12))
                        .foregroundColor(model.timeIsAccent ? Theme.accent : Theme.muted)
                }
                HStack(spacing: Spacing.sm) {
                    Text(model.subtitle)
                        .font(.system(size: 15))
                        .foregroundColor(Theme.muted)
                        .lineLimit(1)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .accessibilityIdentifier("chat-subtitle")
                    HStack(spacing: 6) {
                        if model.muted { Image(systemName: "bell.slash").font(.system(size: 14)).foregroundColor(Theme.muted) }
                        if model.pinned { Image(systemName: "pin").font(.system(size: 14)).foregroundColor(Theme.muted) }
                        if model.unread > 0 {
                            Text("\(model.unread)")
                                .font(.system(size: 12, weight: .bold))
                                .foregroundColor(Theme.onAccent)
                                .padding(.horizontal, 6)
                                .frame(minWidth: 22, minHeight: 22)
                                .background(Capsule().fill(model.badgeIsMuted ? Theme.muted : Theme.accent))
                        }
                    }
                }
            }
        }
        .padding(.horizontal, Spacing.lg)
        .padding(.vertical, Spacing.md)
    }
}
