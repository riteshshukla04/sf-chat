import SwiftUI

@main
struct ChatterApp: App {
    init() {
        // Crash reporting first, so errors during startup are captured too.
        SentryService.start()
    }

    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
