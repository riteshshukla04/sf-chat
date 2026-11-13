# Chatter

A chat app for the Software Factory demo: chat list, direct and group conversations, new chat, calls and settings. Expo SDK 58, React Native 0.88, Expo Router.

There is no server. Chats live on the phone (AsyncStorage) and replies to your messages are simulated. Crashes are reported to Sentry when `EXPO_PUBLIC_SENTRY_DSN` is set at build time, and the app gets over-the-air updates from EAS on the `chat` channel.

## Run

```bash
npm ci
npx expo start            # Expo Go / dev client
npm test                  # jest (src/lib/__tests__)
npx tsc --noEmit
```

## Build an APK for a phone

```bash
npx expo prebuild --platform android --no-install
EXPO_PUBLIC_SENTRY_DSN=... ./android/gradlew -p android assembleRelease -PreactNativeArchitectures=arm64-v8a
adb install -r android/app/build/outputs/apk/release/app-release.apk
```

## Three apps, one repository

| App | Folder | Stack | Build from the command line |
| --- | --- | --- | --- |
| React Native (reference) | `src/`, `app.config.ts` | Expo SDK 58, Expo Router | `npm ci && npm test && npx tsc --noEmit` (APK: see above) |
| Android native | `android-native/` | Kotlin, Jetpack Compose | `cd android-native && ./gradlew testDebugUnitTest assembleDebug` (JDK 17, Android SDK) |
| iOS native | `ios-native/` | Swift, SwiftUI | Logic and tests: `cd ios-native && swift test` (or the `swift:6.0` Docker image). App: `xcodegen generate && xcodebuild -project Chatter.xcodeproj -scheme Chatter ...` on macOS with Xcode |

All three have the same screens (Chats, Calls, Settings, conversation `chat/{id}`, new-chat modal, error screen), the same sample data, and the same `chatter:v1` JSON shape on the device. None has a backend: chats live on the device and replies are simulated. The only outside services are optional Sentry (DSN from the environment, no-op when unset) and, for the RN app only, expo-updates (OTA).

The Android debug build installs as `dev.softwarefactory.chatter.native`, so it sits next to the RN app. See [docs/native-parity.md](docs/native-parity.md) for the screen-by-screen parity checklist and the known gaps, `.maestro/send-message.yaml` (RN) and `.maestro/send-message-native.yaml` (native Android) for the end-to-end flow.

## For agents

See [AGENTS.md](AGENTS.md): how to build on a Linux box, reproduce a crash report on a device, and what a finished change includes.
