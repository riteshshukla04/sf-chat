import SwiftUI
import ChatterCore

/// PLACEHOLDER: the calls task replaces this file (RN: src/app/(tabs)/calls.tsx).
struct CallsScreen: View {
    @Environment(\.colors) private var c

    var body: some View {
        VStack {
            Text("Calls").font(.title.bold()).foregroundColor(c.text)
            Text("Placeholder").foregroundColor(c.muted)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(c.surface)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestID.callsScreen)
    }
}
