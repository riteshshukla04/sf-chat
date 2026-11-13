# Native parity checklist

Reference: the React Native (Expo) app in `src/`. Native apps: `android-native/` (Kotlin, Compose) and `ios-native/` (Swift, SwiftUI).

**How each item was checked**
- **Android**: on a device (redroid, Android 15 x86_64; see "Device" below), light and dark, with the screenshots from the PR. Logic also by unit tests.
- **iOS**: code reading against the RN source plus the Swift unit tests (`swift test`, 56 tests, run on Linux in the `swift:6.0` image). **The iOS app itself could not be built or run here: there is no macOS/Xcode on this machine**, so every iOS UI item is "code review only" and still needs a run on a Mac.
- The RN app was not run on the device in this task; its behaviour is taken from its source.

No backend: the app has no server. Chats live on the device (AsyncStorage key `chatter:v1`), replies are simulated by timers. The only outside services are optional Sentry (DSN from env, no-op when unset; **the native apps have only the no-op reporter, no Sentry SDK yet**) and expo-updates (OTA; not applicable natively, native shows "built in" / channel `none`). No endpoints were invented.

Legend: PASS, FIXED (was different, fixed in this PR), GAP (different, listed below), n/v (not verified on a device).

| # | Item | Android | iOS |
|---|------|---------|-----|
| 1 | Tabs: Chats / Calls / Settings, order, titles, testIDs `tab-*` | PASS | PASS (code) |
| 2 | Tab badge = unread of non-muted chats, hidden at 0 | PASS | PASS (code) |
| 3 | Tab / accent colour light (#1A73E8) and dark (#4C9AFF) | FIXED (was green #128C5E / #21C07A, from before RN's blue-accent change #21) | FIXED (same) |
| 4 | Full palette (background, surface, surfaceAlt, chatBackground, text, muted, border, bubbles, ticks, danger), light and dark | PASS (hex compared; dark checked on device) | PASS (hex, unit test) |
| 5 | Launcher icon background | FIXED (blue) | n/a |
| 6 | Chats list: title "Chatter" in accent, search "Search", empty text `No chats match “q”` | PASS | PASS (code) |
| 7 | List ordering: pinned first, then latest activity; search by title or message | PASS | PASS (unit tests) |
| 8 | Row: avatar colour/initials, time format (HH:mm / Yesterday / weekday / dd/MM/yyyy), time accent when unread and not muted | PASS | PASS (code + tests) |
| 9 | Row preview: `You:` prefix, group sender first name, `Group created. Say hi!`, `No messages yet` | PASS | PASS |
| 10 | Status prefix on own last message: ✓✓ read, 🕓 sending, ✓ otherwise | PASS | PASS |
| 11 | Unread badge (accent; grey when muted), muted and pin icons | PASS | PASS (code) |
| 12 | Tap opens `chat/{id}`; long-press toggles pin | PASS | PASS (code) |
| 13 | New chat FAB `new-chat` opens modal | PASS | PASS |
| 14 | New chat modal title "New chat" | FIXED (header with title and close was missing) | PASS |
| 15 | New chat: "Contacts on Chatter", contacts sorted by name, name + about, tap starts or reuses the direct chat and opens it | PASS | PASS (code) |
| 16 | Conversation header: avatar, title, `chat-subtitle` (online / last seen … / members + last message) | PASS | PASS (code + tests) |
| 17 | Group with no messages: subtitle no longer throws (fixed in RN by #1) | PASS (stale comment in `Chats.kt` corrected) | PASS |
| 18 | Day headers (Today / Yesterday / weekday / date), clock on bubbles, sender name in groups | PASS | PASS (code) |
| 19 | Bubble colours mine/theirs, chat background, light and dark | PASS | PASS (code) |
| 20 | Empty chat notice "Messages are kept on this phone. Say hi 👋" | PASS (dark shot of Weekend trip) | PASS (code) |
| 21 | Input placeholder "Message", send disabled for blank or > 4000 chars, `message-input`, `send` | PASS | PASS |
| 22 | Tick progression sending (clock) → sent (✓) → delivered (✓✓ grey) → read (✓✓ blue) at 400/1200/2600 ms | PASS (`ticks-*` tags; screenshots 04, 05) | GAP 1 |
| 23 | Simulated reply after 4200 ms, direct chats only, text from the same list | PASS (reply "Nice!" in shot 06) | PASS (unit tests) |
| 24 | Opening a chat marks it read (unread → 0) | PASS | PASS |
| 25 | Missing chat text "This chat doesn't exist any more." | PASS | PASS |
| 26 | Calls: title, rows in stored order, red missed, direction icon, voice/video icon, "Unknown" | PASS | PASS (code); direction icons are SF Symbols, not identical glyphs (GAP 2) |
| 27 | Settings: profile, rows (Account, Notifications, Storage, Version, Update channel, Crash reporting) and their text | PASS | PASS (code + tests) |
| 28 | Settings Version "1.0.0 (built in)", channel "none", crash "off (no DSN)" | PASS (RN shows its Expo values; OTA does not exist natively) | PASS |
| 29 | Reset chats: confirm "Reset chats?" / "Back to the sample conversations." / Cancel / Reset, seed restored | PASS (shot 10, 18) | PASS (code) |
| 30 | Profile editing from Settings | EXTRA: RN has no UI for it (store has `updateProfile`) | EXTRA (same) |
| 31 | Persistence across restart, same `chatter:v1` JSON shape | PASS (force-stop, relaunch: sent message and read state kept, shot 17) | PASS (unit tests; n/v on device) |
| 32 | Seed data (contacts, chats, calls, relative times) | PASS (unit test) | PASS (unit test) |
| 33 | Error screen: "Something went wrong", message, "Try again", always light palette, testIDs | PASS (code; not triggered on device) | PASS (code) |
| 34 | Error screen trigger | GAP 3 | GAP 3 |
| 35 | Sentry | GAP 4 | GAP 4 |
| 36 | Dark mode follows system | PASS (shots 12–16) | PASS (code) |
| 37 | Stable testIDs (`chat-row-*`, `chat-subtitle`, `message-input`, `send`, `new-chat`, `tab-*`, `error-*`) | PASS (as resource ids) | PASS (accessibility identifiers; n/v) |

## Known gaps (not fixed here)
1. **iOS delivered/read ticks**: SF Symbols has no double-check glyph; iOS uses `checkmark.circle.fill` for both delivered and read (RN: `check-all`). Needs a custom symbol or two overlaid checkmarks. Cannot be verified without Xcode.
2. **iOS call direction icons** are SF Symbols (`phone.arrow.up.right` …), close to but not the same as MaterialCommunityIcons.
3. **Error screen trigger**: RN's boundary catches any render error. Android shows it only when hydration throws; iOS only on explicit use (SwiftUI/Compose have no render-error boundary). RN, when hydration fails, shows nothing; native shows the error screen, which is a deliberate improvement.
4. **Sentry SDK** is not integrated in either native app (reporter interface exists, no-op).

## Builds and tests (this machine)
| What | Result |
|------|--------|
| RN: `npm test` | PASS, 15 tests |
| RN: `npx tsc --noEmit` | PASS |
| Android: `./gradlew testDebugUnitTest assembleDebug` | PASS (5 test classes, 49 tests; APK built) |
| Android instrumented tests (`androidTest`) | not run |
| iOS logic: `swift test` in the `swift:6.0` Docker image | PASS, 56 tests |
| iOS app (`xcodegen` + `xcodebuild`) | **NOT BUILT: no macOS / Xcode here** |

## Device (Android)
The lender (`devicectl lease lend`) first answered "try again" repeatedly, then failed with "Device failed to boot" (exit 1), so a local `redroid` (Android 15, x86_64) was used instead. The debug build installs as `dev.softwarefactory.chatter.native`.
`.maestro/send-message-native.yaml` is the native version of `send-message.yaml` (same steps, `appId: dev.softwarefactory.chatter.native`). The Maestro CLI is not installed here, so the same steps were driven with `adb` (open `chat-row-asha`, see the subtitle, type "Looks good, approving now", send, see the text in the list): PASS.
