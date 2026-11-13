import ChatterCore
import SwiftUI

/// Pure label/icon decisions for a call row, kept out of the view so they can be unit tested.
/// Mirrors src/app/(tabs)/calls.tsx.
enum CallsLogic {
    static func name(for call: Call, contacts: [String: Contact]) -> String {
        contacts[call.contactId]?.name ?? "Unknown"
    }

    static func isMissed(_ call: Call) -> Bool { call.direction == .missed }

    /// SF Symbol for the direction (call-made / call-received / call-missed in the RN app).
    static func directionSymbol(_ direction: CallDirection) -> String {
        switch direction {
        case .outgoing: return "phone.arrow.up.right"
        case .incoming: return "phone.arrow.down.left"
        case .missed: return "phone.arrow.down.left"
        }
    }

    /// Trailing icon: video or phone.
    static func trailingSymbol(_ call: Call) -> String { call.video ? "video" : "phone" }

    static func directionLabel(_ direction: CallDirection) -> String {
        switch direction {
        case .outgoing: return "Outgoing"
        case .incoming: return "Incoming"
        case .missed: return "Missed"
        }
    }

    static func timeLabel(_ call: Call, now: Int64) -> String { formatTime(call.at, now) }
}

struct CallsScreen: View {
    let openChat: (String) -> Void
    @Environment(AppStore.self) private var store

    var body: some View {
        VStack(spacing: 0) {
            Text("Calls")
                .font(.system(size: 26, weight: .bold))
                .foregroundColor(Theme.text)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.horizontal, Spacing.lg)
                .padding(.vertical, Spacing.sm)
                .accessibilityAddTraits(.isHeader)
            TimelineView(.periodic(from: .now, by: 30)) { context in
                let now = Int64(context.date.timeIntervalSince1970 * 1000)
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(store.calls) { call in
                            row(call, now: now)
                        }
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Theme.surface)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("calls-screen")
    }

    private func row(_ call: Call, now: Int64) -> some View {
        let name = CallsLogic.name(for: call, contacts: store.contacts)
        let missed = CallsLogic.isMissed(call)
        let tint = missed ? Theme.danger : Theme.accent
        return HStack(spacing: Spacing.md) {
            AvatarView(name: name, size: 44)
            VStack(alignment: .leading, spacing: 2) {
                Text(name)
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(missed ? Theme.danger : Theme.text)
                HStack(spacing: 4) {
                    Image(systemName: CallsLogic.directionSymbol(call.direction))
                        .font(.system(size: 13))
                        .foregroundColor(tint)
                        .accessibilityLabel(CallsLogic.directionLabel(call.direction))
                    Text(CallsLogic.timeLabel(call, now: now))
                        .foregroundColor(Theme.muted)
                }
            }
            Spacer(minLength: 0)
            Image(systemName: CallsLogic.trailingSymbol(call))
                .font(.system(size: 20))
                .foregroundColor(Theme.accent)
        }
        .padding(.horizontal, Spacing.lg)
        .padding(.vertical, Spacing.md)
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("call-\(call.id)")
    }
}
