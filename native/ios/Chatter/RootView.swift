import SwiftUI

/// Navigation shell: tabs inside a stack (chats push above the tab bar, like the RN Stack over
/// (tabs)), and a sheet for "New chat". Screen tasks only replace files in Screens/.
struct RootView: View {
    @StateObject private var shell = ShellState()
    @State private var path: [Route] = []
    @State private var showNewChat = false

    var body: some View {
        if let message = shell.error {
            ErrorScreen(message: message, retry: shell.retry)
        } else {
            NavigationStack(path: $path) {
                tabs
                    .navigationDestination(for: Route.self) { route in
                        switch route {
                        case .chat(let id):
                            ChatScreen(chatId: id)
                                .navigationTitle("")
                                .navigationBarTitleDisplayMode(.inline)
                        }
                    }
            }
            .tint(Theme.accent)
            .sheet(isPresented: $showNewChat) {
                NavigationStack {
                    NewChatScreen(openChat: openChatFromSheet, dismiss: { showNewChat = false })
                        .navigationTitle("New chat")
                        .navigationBarTitleDisplayMode(.inline)
                }
                .tint(Theme.accent)
            }
        }
    }

    private var tabs: some View {
        TabView {
            ChatsScreen(openChat: openChat, newChat: { showNewChat = true })
                .tabItem {
                    Label("Chats", systemImage: "message")
                        .accessibilityIdentifier("tab-chats")
                }
                .badge(shell.unread)
            CallsScreen(openChat: openChat)
                .tabItem {
                    Label("Calls", systemImage: "phone")
                        .accessibilityIdentifier("tab-calls")
                }
            SettingsScreen()
                .tabItem {
                    Label("Settings", systemImage: "gearshape")
                        .accessibilityIdentifier("tab-settings")
                }
        }
    }

    private func openChat(_ id: String) { path.append(.chat(id)) }

    private func openChatFromSheet(_ id: String) {
        showNewChat = false
        path.append(.chat(id))
    }
}
