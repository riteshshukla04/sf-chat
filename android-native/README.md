# Chatter — native Android (Kotlin + Jetpack Compose)

Native port of the React Native app in `src/`. Package `dev.softwarefactory.chatter`; **debug builds use the
applicationId `dev.softwarefactory.chatter.native`** so they install next to the RN app (which has the plain id).
The RN app is untouched. This folder is *not* the git-ignored `/android` that Expo generates.

There is no backend: chats live on the device and replies are simulated. The only outside services are optional
Sentry (DSN at build time, no-op when unset) and expo-updates (OTA, not applicable natively).

## Build and test

Needs JDK 17 and the Android SDK (`ANDROID_HOME` or `local.properties` with `sdk.dir=...`; compileSdk 36).

```bash
cd android-native
./gradlew testDebugUnitTest      # JVM unit tests (domain + store)
./gradlew assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
./gradlew connectedDebugAndroidTest   # Compose UI tests, needs a device/emulator
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n dev.softwarefactory.chatter.native/dev.softwarefactory.chatter.MainActivity
```

## Layout (`app/src/main/java/dev/softwarefactory/chatter/`)

| Path | What |
| --- | --- |
| `domain/Chats.kt`, `domain/Seed.kt` | Port of `src/lib/chats.ts` and `src/data/seed.ts` |
| `state/AppStore.kt` | Port of `src/state/store.ts`; `StateFlow` state, JSON under key `chatter:v1` |
| `state/Env.kt` | Injectable `Clock`, `Scheduler` (+ `FakeScheduler` for tests) |
| `data/KeyValueStorage.kt` | Storage interface, DataStore implementation, in-memory fake |
| `ui/theme/Theme.kt` | `Palette` light/dark + `Spacing` from `src/constants/theme.ts` |
| `ui/NavRoot.kt` | Hydration gate, bottom tabs, routes `chat/{id}` and `new-chat` (modal) |
| `ui/screens/*.kt` | One placeholder file per screen: chats list, conversation, new chat, calls, settings |
| `ui/TestTags.kt` | RN testIDs (exposed as resource-ids) |
| `services/` | `BuildInfo`, `CrashReporter` (no-op; Sentry SDK is a TODO) |

Test tags: `chat-row-<id>`, `chat-subtitle`, `message-input`, `send`, `new-chat`, `tab-chats`, `tab-calls`,
`tab-settings`, `error-screen`, `error-message`, `error-retry`.
