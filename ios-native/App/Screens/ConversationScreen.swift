import SwiftUI
import ChatterCore

/// Conversation (RN: src/app/chat/[id].tsx). Route chat/<id>.
/// Header avatar + title + `chat-subtitle`, day headers, mine/theirs bubbles with clock and ticks,
/// `message-input` + `send`, markRead on open. Live ticks and the simulated reply come from ChatStore.
struct ConversationScreen: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.colors) private var c
    let chatId: String
    @State private var draft = ""

    private var now: Millis { Millis(Date().timeIntervalSince1970 * 1000) }

    var body: some View {
        Group {
            if let chat = model.chat(id: chatId) {
                content(chat)
            } else {
                Text(ConversationViewModel.missingChatText)
                    .foregroundColor(c.muted)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(c.background)
            }
        }
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestID.chatScreen)
        .onAppear { model.store.markRead(chatId) }
    }

    private func content(_ chat: Chat) -> some View {
        let rows = ConversationViewModel.rows(chat: chat, contacts: model.state.contacts, now: now)
        return VStack(spacing: 0) {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 6) {
                        ForEach(rows, id: \.key) { row in
                            rowView(row).id(row.key)
                        }
                        if ConversationViewModel.showsEmptyNotice(chat) {
                            Text(ConversationViewModel.emptyNotice)
                                .foregroundColor(c.muted).multilineTextAlignment(.center)
                                .padding(CGFloat(Spacing.md))
                                .background(c.surfaceAlt).cornerRadius(8)
                                .padding(CGFloat(Spacing.lg))
                        }
                    }
                    .padding(CGFloat(Spacing.md))
                }
                .onAppear { scrollToEnd(proxy, rows, animated: false) }
                .onChange(of: rows.last?.key) { _ in scrollToEnd(proxy, rows, animated: true) }
            }
            inputBar(chat)
        }
        .background(c.chatBackground.ignoresSafeArea())
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) { header(chat) }
        }
    }

    private func scrollToEnd(_ proxy: ScrollViewProxy, _ rows: [ConversationRow], animated: Bool) {
        guard let last = rows.last?.key else { return }
        if animated { withAnimation { proxy.scrollTo(last, anchor: .bottom) } } else { proxy.scrollTo(last, anchor: .bottom) }
    }

    private func header(_ chat: Chat) -> some View {
        HStack(spacing: CGFloat(Spacing.sm)) {
            avatar(chat)
            VStack(alignment: .leading, spacing: 0) {
                Text(chat.title).font(.system(size: 17, weight: .semibold)).foregroundColor(c.text).lineLimit(1)
                Text(ConversationViewModel.subtitle(chat: chat, contacts: model.state.contacts, now: now))
                    .font(.system(size: 12)).foregroundColor(c.muted).lineLimit(1)
                    .accessibilityIdentifier(TestID.chatSubtitle)
            }
        }
    }

    private func avatar(_ chat: Chat) -> some View {
        ZStack {
            Circle().fill(c.accent)
            if chat.kind == .group {
                Image(systemName: "person.2.fill").font(.system(size: 16)).foregroundColor(c.onAccent)
            } else {
                Text(initials(chat.title)).font(.system(size: 14, weight: .semibold)).foregroundColor(c.onAccent)
            }
        }
        .frame(width: 36, height: 36)
        .accessibilityHidden(true)
    }

    @ViewBuilder private func rowView(_ row: ConversationRow) -> some View {
        switch row {
        case .day(_, let label):
            Text(label).font(.system(size: 12, weight: .semibold)).foregroundColor(c.muted)
                .padding(.horizontal, CGFloat(Spacing.md)).padding(.vertical, 4)
                .background(c.surfaceAlt).cornerRadius(8)
                .padding(.vertical, CGFloat(Spacing.sm))
        case .message(_, let b):
            bubble(b)
        }
    }

    private func bubble(_ b: MessageBubble) -> some View {
        HStack(spacing: 0) {
            if b.mine { Spacer(minLength: 48) }
            VStack(alignment: .leading, spacing: 2) {
                if let name = b.senderName {
                    Text(name).font(.system(size: 13, weight: .bold)).foregroundColor(c.accent)
                }
                Text(b.text).font(.system(size: 16)).foregroundColor(c.text)
                HStack(spacing: 4) {
                    Text(b.clock).font(.system(size: 11)).foregroundColor(c.muted)
                    if let tick = b.tick, let tid = b.tickTestID {
                        Image(systemName: icon(tick)).font(.system(size: 12))
                            .foregroundColor(b.tickIsRead ? c.tickRead : c.muted)
                            .accessibilityIdentifier(tid)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .trailing)
            }
            .padding(.horizontal, CGFloat(Spacing.md)).padding(.top, 6).padding(.bottom, 4)
            .background(b.mine ? c.bubbleMine : c.bubbleTheirs)
            .clipShape(RoundedRectangle(cornerRadius: 12))
            .fixedSize(horizontal: false, vertical: true)
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier(b.testID)
            if !b.mine { Spacer(minLength: 48) }
        }
    }

    private func icon(_ t: TickIcon) -> String {
        switch t {
        case .clock: return "clock"
        case .check: return "checkmark"
        case .checkAll: return "checkmark.circle.fill"
        }
    }

    private func inputBar(_ chat: Chat) -> some View {
        HStack(alignment: .bottom, spacing: CGFloat(Spacing.sm)) {
            TextField("Message", text: $draft, axis: .vertical)
                .lineLimit(1...5)
                .foregroundColor(c.text)
                .padding(.horizontal, CGFloat(Spacing.lg)).padding(.vertical, 10)
                .background(c.surface).cornerRadius(22)
                .accessibilityIdentifier(TestID.messageInput)
            Button {
                guard canSend(draft) else { return }
                model.store.sendMessage(chat.id, draft)
                draft = ""
            } label: {
                Image(systemName: "paperplane.fill").font(.system(size: 20)).foregroundColor(c.onAccent)
                    .frame(width: 46, height: 46).background(c.accent).clipShape(Circle())
                    .opacity(canSend(draft) ? 1 : 0.5)
            }
            .disabled(!canSend(draft))
            .accessibilityLabel("Send")
            .accessibilityIdentifier(TestID.send)
        }
        .padding(CGFloat(Spacing.sm))
    }
}
