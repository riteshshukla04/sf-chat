import SwiftUI

/// Stub. Calls task replaces this file (identifier: calls-screen).
struct CallsScreen: View {
    let openChat: (String) -> Void

    var body: some View {
        Text("Calls").font(.title2.bold()).foregroundColor(Theme.text)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Theme.background)
            .accessibilityElement(children: .contain)
            .accessibilityIdentifier("calls-screen")
    }
}
