This is an Expo/React Native mobile application. Prioritize mobile-first patterns, performance, and cross-platform compatibility.

## Chatter specifics

- Chatter is a chat app: a chat list, conversations (direct and group), new chat, calls and settings. There is no server: chats live on the phone (AsyncStorage) and replies are simulated (`src/state/store.ts`).
- Logic lives in plain TypeScript in `src/lib/chats.ts`. Fix bugs there and add a jest test next to it in `src/lib/__tests__/`.
- Before finishing: `npx tsc --noEmit` and `npm test` must pass.
- Keep the `testID`s stable (`chat-row-<id>`, `chat-subtitle`, `message-input`, `send`, `new-chat`, `tab-chats`, `tab-calls`, `tab-settings`, `error-screen`, `error-message`). `.maestro/` flows use them.

## Building and checking on a Linux agent box (agentvm, redroid)

`android/` is generated and git-ignored, so generate it before building. redroid is x86_64; a phone is arm64-v8a.

```bash
npm ci --no-audit --no-fund
npx expo prebuild --platform android --no-install      # creates ./android
# Release-like build: the JS is bundled in, no Metro needed; signed with the debug keystore.
APK=$(/opt/agentvm/skills-scripts/apk-build.sh --src . --variant release --abi x86_64 --no-install | tail -1)
adb install -r "$APK"
adb shell am start -n dev.softwarefactory.chatter/.MainActivity
```

- The app opens on the Chats tab with sample conversations (Settings → Reset chats brings them back).
- **Reproducing a crash report.** A render error shows the root `ErrorBoundary` (`src/app/_layout.tsx`): "Something went wrong" with the error message (testIDs `error-screen`, `error-message`), and it is reported to Sentry. logcat shows it under `ReactNativeJS`. Read the Sentry event's stack trace and breadcrumbs (`open chat` carries the chat id) to find the screen.
- A correct fix keeps the screen working for every kind of chat (direct and group, with and without messages), and comes with a jest test in `src/lib/__tests__/` that fails without the fix.
- Builds take minutes: run them once per state (before the fix, after the fix), and use the `agent-heavy` lock the build script already takes.

## Expo has changed — do not trust your training data

Expo ships breaking changes every SDK release. APIs you remember are likely renamed, moved, or removed. Before writing any code that touches an Expo, EAS, or React Native API:

1. Read the major version of the `expo` package in `package.json`.
2. Fetch the matching versioned docs: `https://docs.expo.dev/versions/v<major>.0.0/`
3. For anything else, fetch https://docs.expo.dev/llms.txt — an index of all Expo docs with corrections to common LLM misconceptions. Follow its links to the specific page you need; never answer from memory.

## Commands

Use `bunx` instead of `npx` if the project uses bun (`bun.lock` present).

```bash
npx expo install <package>  # ALWAYS use instead of npm/yarn/pnpm/bun add — resolves SDK-compatible versions
npx expo start              # start the dev server
npx expo lint               # lint
npx tsc --noEmit            # typecheck
npx expo-doctor             # diagnose dependency and config issues
npx expo install --fix      # fix incompatible package versions
```

Run lint and typecheck before declaring any task done.

## Navigation & Routing

- Use **Expo Router** for all navigation. Routes live in `src/app/` — every file there is a screen, `_layout.tsx` files define navigators. Keep non-route code (components, hooks, utils) outside `src/app/`.
- Import `Link`, `router`, and `useLocalSearchParams` from `expo-router`.
- Docs: https://docs.expo.dev/router/introduction.md

## Building with EAS

Use EAS to build, sign, and submit the app in the cloud (`eas build`, `eas submit`) and to ship over-the-air updates (`eas update`) — no local Xcode or Android Studio required. Run EAS CLI as `bunx eas-cli <command>` in Bun projects, or `npx eas-cli@latest <command>` otherwise; substitute that for bare `eas` in docs examples.
Docs: https://docs.expo.dev/eas/index.md

## Rules

- If `ios/` and `android/` directories do not exist, they are generated (Continuous Native Generation). Never create or edit them by hand — configure native behavior in `app.json` and config plugins.
- Expo Go only includes its bundled native modules. After adding a library with native code, the app needs a development build: `npx expo run:ios|android` locally, or `eas build --profile development`.
- Prefer recommended Expo modules over third-party libraries, and check your available skills before adding dependencies. Docs: https://docs.expo.dev/versions/latest/index.md
