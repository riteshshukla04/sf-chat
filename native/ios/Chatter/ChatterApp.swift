import ChatterCore
import SwiftUI

@main
struct ChatterApp: App {
    /// One store for the whole app, shared through the SwiftUI environment:
    /// `@Environment(AppStore.self) private var store`.
    @State private var store = AppStore()

    init() {
        // Crash reporting first, so errors during startup are captured too.
        SentryService.start()
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(store)
                .task { if !store.hydrated { store.hydrate() } }
        }
    }
}
