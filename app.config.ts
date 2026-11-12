import type { ConfigContext, ExpoConfig } from 'expo/config';

/**
 * Build-time configuration. Everything account-specific comes from env vars so
 * the app builds on any machine without an Expo or Sentry account:
 *
 *   EAS_PROJECT_ID   EAS project id. Defaults to the shared demo project; set it empty to turn OTA updates off.
 *   SENTRY_ORG / SENTRY_PROJECT / SENTRY_AUTH_TOKEN
 *                    Only needed to upload source maps from native builds.
 *
 * Runtime (inlined into the JS bundle, see .env.example):
 *   EXPO_PUBLIC_SENTRY_DSN   crash reporting; off when unset.
 */

const APP_ID = 'dev.softwarefactory.chatter';
// Chatter shares an EAS project with another demo app, so it asks for updates on its own channel.
const UPDATE_CHANNEL = 'chat';
// The EAS project. Not a secret; EAS_PROJECT_ID overrides it, and an empty value turns updates off.
// EAS ties a project to its slug, which is why the slug below is the shared project's, not "chatter".
const EAS_PROJECT_ID = process.env.EAS_PROJECT_ID ?? '93bcb19a-1d55-410d-940c-ee41a3395e3a';

const sentryPluginOptions: Record<string, unknown> = {
  // Source maps are only uploaded when a Sentry auth token is present.
  disableAutoUpload: !process.env.SENTRY_AUTH_TOKEN,
};
if (process.env.SENTRY_ORG) sentryPluginOptions.organization = process.env.SENTRY_ORG;
if (process.env.SENTRY_PROJECT) sentryPluginOptions.project = process.env.SENTRY_PROJECT;
if (process.env.SENTRY_URL) sentryPluginOptions.url = process.env.SENTRY_URL;

export default ({ config }: ConfigContext): ExpoConfig => ({
  ...config,
  name: 'Chatter',
  slug: 'liftlog',
  version: '1.0.0',
  orientation: 'portrait',
  icon: './assets/images/icon.png',
  scheme: 'chatter',
  userInterfaceStyle: 'automatic',
  ios: {
    bundleIdentifier: APP_ID,
    supportsTablet: false,
  },
  android: {
    package: APP_ID,
    versionCode: 1,
    adaptiveIcon: {
      backgroundColor: '#128C5E',
      foregroundImage: './assets/images/android-icon-foreground.png',
      monochromeImage: './assets/images/android-icon-monochrome.png',
    },
    predictiveBackGestureEnabled: false,
  },
  web: {
    output: 'single',
    favicon: './assets/images/favicon.png',
  },
  runtimeVersion: { policy: 'appVersion' },
  updates: EAS_PROJECT_ID
    ? {
        enabled: true,
        url: `https://u.expo.dev/${EAS_PROJECT_ID}`,
        checkAutomatically: 'ON_LOAD',
        fallbackToCacheTimeout: 0,
        requestHeaders: { 'expo-channel-name': UPDATE_CHANNEL },
      }
    : { enabled: false },
  extra: {
    updateChannel: UPDATE_CHANNEL,
    ...(EAS_PROJECT_ID ? { eas: { projectId: EAS_PROJECT_ID } } : {}),
  },
  plugins: [
    'expo-router',
    [
      'expo-splash-screen',
      {
        backgroundColor: '#128C5E',
        image: './assets/images/splash-icon.png',
        imageWidth: 96,
      },
    ],
    ['@sentry/react-native/expo', sentryPluginOptions],
  ],
  experiments: {
    typedRoutes: false,
    reactCompiler: true,
  },
});
