import SwiftUI

/// Matches ErrorBoundary in src/app/_layout.tsx. Always uses the light palette, like the original.
struct ErrorScreen: View {
    let message: String
    let retry: () -> Void

    private let bg = Color(uiColor: UIColor(hex: Theme.backgroundPair.light))
    private let fg = Color(uiColor: UIColor(hex: Theme.textPair.light))
    private let accent = Color(uiColor: UIColor(hex: Theme.accentPair.light))

    var body: some View {
        VStack(alignment: .leading, spacing: Spacing.md) {
            Spacer()
            Text("Something went wrong")
                .font(.system(size: 22, weight: .bold))
                .foregroundColor(fg)
            Text(message)
                .foregroundColor(fg)
                .accessibilityIdentifier("error-message")
            Button(action: retry) {
                Text("Try again")
                    .fontWeight(.semibold)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(14)
                    .background(RoundedRectangle(cornerRadius: 10).fill(accent))
            }
            .accessibilityIdentifier("error-retry")
            Spacer()
        }
        .padding(Spacing.xl)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(bg.ignoresSafeArea())
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier("error-screen")
    }
}
