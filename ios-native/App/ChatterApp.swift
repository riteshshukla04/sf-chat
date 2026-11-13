import SwiftUI
import ChatterCore

@main
struct ChatterApp: App {
    @StateObject private var model = AppModel()

    var body: some Scene {
        WindowGroup {
            ThemedRoot { RootView().environmentObject(model) }
        }
    }
}
