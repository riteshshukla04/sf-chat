import SwiftUI
import ChatterCore

/// PLACEHOLDER: the settings task replaces this file (RN: src/app/(tabs)/settings.tsx).
struct SettingsScreen: View {
    @Environment(\.colors) private var c

    var body: some View {
        VStack {
            Text("Settings").font(.title.bold()).foregroundColor(c.text)
            Text("Placeholder").foregroundColor(c.muted)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(c.surface)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestID.settingsScreen)
    }
}
