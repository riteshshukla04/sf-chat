import SwiftUI
import ChatterCore

/// Calls tab (RN: src/app/(tabs)/calls.tsx): title and a row per call, in stored order.
struct CallsScreen: View {
    @EnvironmentObject private var model: AppModel
    @Environment(\.colors) private var c

    var body: some View {
        let rows = callRows(model.state.calls, contacts: model.state.contacts,
                            now: Millis((Date().timeIntervalSince1970 * 1000).rounded()))
        VStack(spacing: 0) {
            Text("Calls").font(.system(size: 26, weight: .bold)).foregroundColor(c.text)
                .accessibilityAddTraits(.isHeader)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, CGFloat(Spacing.lg)).padding(.vertical, CGFloat(Spacing.sm))
            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(rows, id: \.id) { row in CallRow(row: row) }
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(c.surface)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestID.callsScreen)
    }
}

private struct CallRow: View {
    let row: CallRowModel
    @Environment(\.colors) private var c

    var body: some View {
        HStack(spacing: CGFloat(Spacing.md)) {
            AvatarView(name: row.name, size: 44)
            VStack(alignment: .leading, spacing: 2) {
                Text(row.name).font(.system(size: 16, weight: .semibold)).foregroundColor(row.missed ? c.danger : c.text)
                HStack(spacing: 4) {
                    Image(systemName: row.directionSymbol).font(.system(size: 13))
                        .foregroundColor(row.missed ? c.danger : c.accent)
                    Text(row.time).font(.system(size: 15)).foregroundColor(c.muted)
                }
            }
            Spacer(minLength: 0)
            Image(systemName: row.kindSymbol).font(.system(size: 20)).foregroundColor(c.accent)
        }
        .padding(.horizontal, CGFloat(Spacing.lg)).padding(.vertical, CGFloat(Spacing.md))
        .accessibilityElement(children: .combine)
    }
}
