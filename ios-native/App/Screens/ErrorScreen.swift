import SwiftUI
import ChatterCore

/// RN's root ErrorBoundary screen (src/app/_layout.tsx): "Something went wrong", the error message and
/// "Try again". Always in the light palette, like RN. The error is reported to the crash reporter
/// (the Sentry wrapper) when the screen appears. SwiftUI has no render-error boundary, so screens that
/// can fail show this with a retry action.
struct ErrorScreen: View {
    let error: Error
    let reporter: CrashReporter
    let retry: () -> Void

    init(error: Error, reporter: CrashReporter, retry: @escaping () -> Void) {
        self.error = error; self.reporter = reporter; self.retry = retry
    }

    /// For a plain message (no underlying error).
    init(message: String, reporter: CrashReporter, retry: @escaping () -> Void) {
        struct Message: LocalizedError { let errorDescription: String? }
        self.init(error: Message(errorDescription: message), reporter: reporter, retry: retry)
    }

    var body: some View {
        let light = ThemeColors.light
        VStack(alignment: .leading, spacing: CGFloat(Spacing.md)) {
            Text(ErrorScreenText.title).font(.system(size: 22, weight: .bold)).foregroundColor(light.text)
            Text(errorMessageText(error)).foregroundColor(light.text).accessibilityIdentifier(TestID.errorMessage)
            Button(action: retry) {
                Text(ErrorScreenText.retry).fontWeight(.semibold).foregroundColor(.white)
                    .frame(maxWidth: .infinity).padding(14)
                    .background(light.accent).cornerRadius(10)
            }
            .accessibilityIdentifier(TestID.errorRetry)
        }
        .padding(CGFloat(Spacing.xl))
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
        .background(light.background)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(TestID.errorScreen)
        .onAppear { reporter.captureException(error) }
    }
}
