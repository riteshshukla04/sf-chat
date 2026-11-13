import SwiftUI
import ChatterCore

/// Bottom tabs: Chats / Calls / Settings (RN: src/app/(tabs)/_layout.tsx), unread badge on Chats.
struct MainTabView: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.colors) private var c

    var body: some View {
        TabView {
            ChatsListScreen()
                .tabItem { Label("Chats", systemImage: "message") }
                .badge(model.unread)
                .accessibilityIdentifier(TestID.tabChats)
            CallsScreen()
                .tabItem { Label("Calls", systemImage: "phone") }
                .accessibilityIdentifier(TestID.tabCalls)
            SettingsScreen()
                .tabItem { Label("Settings", systemImage: "gearshape") }
                .accessibilityIdentifier(TestID.tabSettings)
        }
        .tint(c.accent)
        .toolbar(.hidden, for: .navigationBar)
    }
}
