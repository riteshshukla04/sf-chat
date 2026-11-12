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

## For agents

See [AGENTS.md](AGENTS.md): how to build on a Linux box, reproduce a crash report on a device, and what a finished change includes.
