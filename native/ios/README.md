# Chatter (iOS, SwiftUI)

Native iOS port of the React Native app. iOS 17+, bundle id `dev.softwarefactory.chatter`.
No server: no network calls except optional Sentry crash reporting.

## Layout
- `project.yml` – XcodeGen spec (app target `Chatter`, unit tests `ChatterTests`, UI tests `ChatterUITests`, scheme `Chatter`). The `.xcodeproj` and `Info.plist` are generated and git-ignored.
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

## ChatterCore (non-UI core)
`ChatterCore/` is a local Swift Package: chat logic (`Chats.swift`, port of `src/lib/chats.ts`), seed data, and `AppStore` (`@Observable`, persists JSON under `chatter:v1` in UserDefaults with the same shape as the RN app). Storage, scheduler and clock are injected. Run its tests anywhere with a Swift 5.9+ toolchain: `cd native/ios/ChatterCore && swift test`.

## Release note
`ReleaseNote` in Info.plist comes from the `RELEASE_NOTE` build setting; pass `RELEASE_NOTE="$EXPO_PUBLIC_RELEASE_NOTE"` to xcodebuild to show it under the title (identifier `release-note`).
