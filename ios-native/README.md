# Chatter for iOS (Swift + SwiftUI)

Native port of the React Native app in `../src`. The RN app is untouched.
There is **no backend**: chats live on the device (UserDefaults, key `chatter:v1`, same JSON as the RN app's AsyncStorage) and replies are simulated.

- `Sources/ChatterCore/` (Swift Package, Foundation only): `Theme` (Palette/Spacing/test ids), `Domain/Chats.swift` (all of `src/lib/chats.ts`), `Data/Seed.swift`, `State/ChatStore.swift` (+ injectable `KeyValueStorage`, `Scheduler`, clock), `Services` (build info, no-op crash reporter).
- `Tests/ChatterCoreTests/`: XCTest port of `chats.test.ts`, seed and store tests (fake timers, in-memory storage).
- `App/`: SwiftUI app, iOS 16+, bundle id `dev.softwarefactory.chatter`, name Chatter, portrait, follows system light/dark. One file per screen in `App/Screens/` (all placeholders for now).
- `project.yml`: XcodeGen spec for the app project.

## Commands

```bash
# Unit tests (macOS, or Linux with a Swift 5.9+ toolchain; no Xcode needed)
cd ios-native && swift test
# or on any machine with Docker
docker run --rm -v "$PWD:/src" -w /src swift:6.0 swift test

# App (macOS + Xcode 15+; brew install xcodegen)
cd ios-native && xcodegen generate
xcodebuild -project Chatter.xcodeproj -scheme Chatter -destination 'platform=iOS Simulator,name=iPhone 15' build
xcrun simctl install booted <DerivedData>/Build/Products/Debug-iphonesimulator/Chatter.app
xcrun simctl launch booted dev.softwarefactory.chatter
```

Crash reporting: `CrashReporter` is a no-op. TODO: add sentry-cocoa and wire it when `SENTRY_DSN` (env) or the `SentryDSN` Info.plist key is set.
