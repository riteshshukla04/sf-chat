import SwiftUI
import ChatterCore

/// Hydration gate (RN: RootLayout renders nothing until `hydrated`), then the chat stack:
/// tabs at the root, route chat/<id> pushed, new-chat as a modal.
struct RootView: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.colors) private var c

    var body: some View {
        Group {
            if model.state.hydrated {
                NavigationStack(path: $model.path) {
                    MainTabView()
                        .navigationDestination(for: String.self) { id in ConversationScreen(chatId: id) }
                }
                .sheet(isPresented: $model.showingNewChat) {
                    NavigationStack { NewChatScreen() }
                }
            } else {
                c.background.ignoresSafeArea()
            }
        }
        .onAppear { if !model.state.hydrated { model.store.hydrate() } }
    }
}
