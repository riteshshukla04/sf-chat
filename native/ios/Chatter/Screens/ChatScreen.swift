import ChatterCore
import SwiftUI

/// Conversation. Port of src/app/chat/[id].tsx. The header (avatar + title + subtitle) goes in the
/// principal toolbar slot; RootView leaves the navigation title blank.
struct ChatScreen: View {
    let chatId: String
    @Environment(AppStore.self) private var store
    @State private var draft = ""
    @State private var picker = PickerState()
    @State private var now = Int64(Date().timeIntervalSince1970 * 1000)
    private let minuteTick = Timer.publish(every: 60, on: .main, in: .common).autoconnect()

    var body: some View {
        Group {
            if let chat = store.chatById(chatId) {
                conversation(chat)
            } else {
                Text("This chat doesn't exist any more.")
                    .foregroundColor(Theme.muted)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Theme.background)
            }
        }
        .onAppear {
            SentryService.addBreadcrumb("open chat", data: ["chat": chatId])
            store.markRead(chatId)
        }
        .onReceive(minuteTick) { _ in now = Int64(Date().timeIntervalSince1970 * 1000) }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("chat-screen")
    }

    @ViewBuilder
    private func conversation(_ chat: Chat) -> some View {
        let rows = chatRows(chat.messages, now)
        VStack(spacing: 0) {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 6) {
                        ForEach(rows) { row in
                            switch row {
                            case .day(_, let label): dayPill(label)
                            case .message(let m): messageRow(m, chat)
                            }
                        }
                        if chat.messages.isEmpty {
                            Text("Messages are kept on this phone. Say hi 👋")
                                .foregroundColor(Theme.muted)
                                .multilineTextAlignment(.center)
                                .padding(Spacing.md)
                                .background(RoundedRectangle(cornerRadius: 8).fill(Theme.surfaceAlt))
                                .padding(Spacing.lg)
                        }
                    }
                    .padding(Spacing.md)
                }
                .defaultScrollAnchor(.bottom)
                .onChange(of: rows.last?.id) { _, last in
                    guard let last else { return }
                    withAnimation { proxy.scrollTo(last, anchor: .bottom) }
                }
            }
            inputBar(chat)
        }
        .background(Theme.chatBackground)
        .toolbar {
            ToolbarItem(placement: .principal) { header(chat) }
        }
    }

    private func header(_ chat: Chat) -> some View {
        HStack(spacing: Spacing.sm) {
            AvatarView(name: chat.title, size: 36, group: chat.kind == .group)
            VStack(alignment: .leading, spacing: 0) {
                Text(chat.title).font(.system(size: 17, weight: .semibold)).foregroundColor(Theme.text).lineLimit(1)
                Text(headerSubtitle(chat, store.contacts, now))
                    .font(.system(size: 12)).foregroundColor(Theme.muted).lineLimit(1)
                    .accessibilityIdentifier("chat-subtitle")
            }
        }
    }

    private func dayPill(_ label: String) -> some View {
        Text(label)
            .font(.system(size: 12, weight: .semibold))
            .foregroundColor(Theme.muted)
            .padding(.horizontal, Spacing.md).padding(.vertical, 4)
            .background(RoundedRectangle(cornerRadius: 8).fill(Theme.surfaceAlt))
            .padding(.vertical, Spacing.sm)
    }

    private func messageRow(_ m: Message, _ chat: Chat) -> some View {
        let mine = m.from == "me"
        let pills = reactionSummary(m)
        return HStack(spacing: 0) {
            if mine { Spacer(minLength: 48) }
            VStack(alignment: mine ? .trailing : .leading, spacing: 4) {
                if picker.open == m.id { reactionPicker(m, chat) }
                bubble(m, chat, mine: mine)
                if !pills.isEmpty {
                    HStack(spacing: 4) {
                        ForEach(pills, id: \.emoji) { p in reactionPill(p, m, chat) }
                    }
                    .padding(.horizontal, 6).padding(.top, -6)
                }
            }
            if !mine { Spacer(minLength: 48) }
        }
        .id(m.id)
    }

    private func bubble(_ m: Message, _ chat: Chat, mine: Bool) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            if chat.kind == .group && !mine {
                Text(firstName(store.contacts[m.from]?.name ?? "Someone"))
                    .font(.system(size: 13, weight: .bold)).foregroundColor(Theme.accent)
            }
            Text(m.text).font(.system(size: 16)).foregroundColor(Theme.text)
            HStack(spacing: 4) {
                Text(formatClock(m.sentAt)).font(.system(size: 11)).foregroundColor(Theme.muted)
                if mine { ticks(m.status) }
            }
            .frame(maxWidth: .infinity, alignment: .trailing)
        }
        .padding(.horizontal, Spacing.md).padding(.top, 6).padding(.bottom, 4)
        .background(
            UnevenRoundedRectangle(
                topLeadingRadius: mine ? 12 : 2, bottomLeadingRadius: 12,
                bottomTrailingRadius: 12, topTrailingRadius: mine ? 2 : 12
            ).fill(mine ? Theme.bubbleMine : Theme.bubbleTheirs)
        )
        .fixedSize(horizontal: false, vertical: true)
        .contentShape(Rectangle())
        .onLongPressGesture(minimumDuration: 0.35) { picker.longPress(m.id) }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("message-\(m.id)")
    }

    @ViewBuilder
    private func ticks(_ status: MessageStatus) -> some View {
        let colour = status == .read ? Theme.tickRead : Theme.muted
        Group {
            switch status {
            case .sending:
                Image(systemName: "clock")
            case .sent:
                Image(systemName: "checkmark")
            case .delivered, .read:
                HStack(spacing: -7) {
                    Image(systemName: "checkmark")
                    Image(systemName: "checkmark")
                }
            }
        }
        .font(.system(size: 11, weight: .semibold))
        .foregroundColor(colour)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(status.rawValue)
        .accessibilityIdentifier("ticks-\(status.rawValue)")
    }

    private func reactionPicker(_ m: Message, _ chat: Chat) -> some View {
        HStack(spacing: 0) {
            ForEach(reactionEmojis, id: \.self) { emoji in
                Button {
                    store.reactToMessage(chat.id, m.id, emoji)
                    picker.close()
                } label: {
                    Text(emoji).font(.system(size: 24)).padding(6)
                }
                .buttonStyle(.plain)
                .accessibilityLabel("React \(emoji)")
                .accessibilityIdentifier("pick-\(emoji)")
            }
        }
        .padding(.horizontal, 6)
        .background(Capsule().fill(Theme.surface).shadow(radius: 2))
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("reaction-picker")
    }

    private func reactionPill(_ p: ReactionPill, _ m: Message, _ chat: Chat) -> some View {
        Button { store.reactToMessage(chat.id, m.id, p.emoji) } label: {
            Text(p.count > 1 ? "\(p.emoji) \(p.count)" : p.emoji)
                .font(.system(size: 13)).foregroundColor(Theme.text)
                .padding(.horizontal, 7).padding(.vertical, 2)
                .background(Capsule().fill(p.mine ? Theme.bubbleMine : Theme.surface))
                .overlay(Capsule().stroke(p.mine ? Theme.accent : Theme.surfaceAlt, lineWidth: 1))
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier("reaction-\(m.id)-\(p.emoji)")
    }

    private func inputBar(_ chat: Chat) -> some View {
        let enabled = canSend(draft)
        return HStack(alignment: .bottom, spacing: Spacing.sm) {
            TextField("Message", text: $draft, axis: .vertical)
                .lineLimit(1...6)
                .font(.system(size: 16))
                .foregroundColor(Theme.text)
                .padding(.horizontal, Spacing.lg).padding(.vertical, 10)
                .frame(maxHeight: 120)
                .background(RoundedRectangle(cornerRadius: 22).fill(Theme.surface))
                .accessibilityIdentifier("message-input")
            Button {
                guard canSend(draft) else { return }
                store.sendMessage(chat.id, draft)
                draft = ""
            } label: {
                Image(systemName: "paperplane.fill")
                    .font(.system(size: 20)).foregroundColor(Theme.onAccent)
                    .frame(width: 46, height: 46)
                    .background(Circle().fill(Theme.accent))
                    .opacity(enabled ? 1 : 0.5)
            }
            .disabled(!enabled)
            .accessibilityLabel("Send")
            .accessibilityIdentifier("send")
        }
        .padding(Spacing.sm)
    }
}
