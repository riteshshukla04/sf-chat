// Sentry's Expo Metro config adds debug IDs to bundles so stack traces can be
// matched to source maps (native builds and EAS Updates alike).
const { getSentryExpoConfig } = require('@sentry/react-native/metro');

module.exports = getSentryExpoConfig(__dirname);
