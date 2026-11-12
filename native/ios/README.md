# Chatter (iOS, SwiftUI)

Native iOS port of the React Native app. iOS 17+, bundle id `dev.softwarefactory.chatter`.
No server: no network calls except optional Sentry crash reporting.

## Layout
- `project.yml` – XcodeGen spec (app target `Chatter`, unit tests `ChatterTests`, scheme `Chatter`). The `.xcodeproj` and `Info.plist` are generated and git-ignored.
- `Chatter/Theme.swift` – palette + spacing mirroring `src/constants/theme.ts`.
- `Chatter/RootView.swift` – tabs, `NavigationStack` (chat), "New chat" sheet. Do not edit from screen tasks.
- `Chatter/Screens/*` – one stub per screen; screen tasks replace only their own file.
- `Chatter/Components/AvatarView.swift`, `ErrorScreen.swift`, `Services/BuildInfo.swift`, `Services/SentryService.swift`.

## Build and test (needs macOS + Xcode 15+ and XcodeGen)
```sh
brew install xcodegen
cd native/ios
xcodegen generate
xcodebuild -scheme Chatter -destination 'platform=iOS Simulator,name=iPhone 15' build test
```

## Crash reporting (optional)
Sentry (sentry-cocoa via SwiftPM) starts only if `SENTRY_DSN` is set when building:
```sh
SENTRY_DSN=https://… xcodegen generate && SENTRY_DSN=https://… xcodebuild -scheme Chatter …
```
Without it, Sentry is never started and nothing is sent.

## Test identifiers
Same as the RN app (see `AGENTS.md`), exposed as `accessibilityIdentifier`. Tab identifiers are set on the tab item labels.
