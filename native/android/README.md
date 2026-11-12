# Chatter – native Android (Kotlin, Jetpack Compose)

Native port of the React Native app. Package / applicationId `dev.softwarefactory.chatter`, min SDK 24, target SDK 35.
The React Native app in `src/` is untouched. The app makes **no network calls** (chats live on device); the only
optional network use is Sentry crash reporting, started only when a DSN is given at build time.

## Build and test

Needs JDK 17 and the Android SDK (`ANDROID_HOME`, or `sdk.dir` in `native/android/local.properties`).

```bash
cd native/android
./gradlew assembleDebug testDebugUnitTest   # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease                   # signed with the debug keystore, like the RN app
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n dev.softwarefactory.chatter/.MainActivity
```

Crash reporting (optional): `EXPO_PUBLIC_SENTRY_DSN=https://… ./gradlew assembleDebug` (falls back to `SENTRY_DSN`).
Without a DSN Sentry never starts.

## Layout

- `ui/theme/Theme.kt` – palettes and `Spacing`, mirrors `src/constants/theme.ts`; follows system dark mode.
- `ui/nav/ChatterApp.kt` – navigation shell: tabs `chats` / `calls` / `settings`, `chat/{id}`, modal `new-chat`.
  Screen tasks never edit it. `ui/nav/Unread.kt` is the placeholder for the Chats tab badge (`totalUnread`).
- `ui/screens/*Screen.kt` – one stub per screen; a screen task replaces only its own file (same signature).
  `ChatScreen` fills the top bar with `ProvideHeader { ChatHeader(...) }`.
- `ui/Avatar.kt` – port of `avatar.tsx`; `ui/ErrorScreen.kt` – port of the root `ErrorBoundary`.
- `MainActivity.kt` – `CrashCatcher` reports an uncaught error, relaunches and shows the error screen.
- `services/BuildInfo.kt`, `services/ErrorReporting.kt` – ports of `build-info.ts` and `sentry.ts`.

Test IDs are Compose `testTag`s exposed as resource ids (`testTagsAsResourceId`), so UiAutomator / Maestro see them.
