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

/// RN's root ErrorBoundary screen. Nothing renders it yet (SwiftUI has no render-error boundary);
/// screens that can fail show it with a retry action.
struct ErrorScreen: View {
    @Environment(\.colors) private var c
    let message: String
    let retry: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: CGFloat(Spacing.md)) {
            Text("Something went wrong").font(.system(size: 22, weight: .bold)).foregroundColor(c.text)
            Text(message).foregroundColor(c.text).accessibilityIdentifier(TestID.errorMessage)
            Button(action: retry) {
                Text("Try again").fontWeight(.semibold).foregroundColor(.white)
                    .frame(maxWidth: .infinity).padding(14)
                    .background(c.accent).cornerRadius(10)
            }
            .accessibilityIdentifier(TestID.errorRetry)
        }
        .padding(CGFloat(Spacing.xl))
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
        .background(Color(hex: Palette.light.background))
        .accessibilityIdentifier(TestID.errorScreen)
    }
}

