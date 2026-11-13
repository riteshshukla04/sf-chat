import { DarkTheme, DefaultTheme, Stack, ThemeProvider, type ErrorBoundaryProps } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import { StatusBar } from 'expo-status-bar';
import { useEffect } from 'react';
import { Pressable, Text, useColorScheme, View } from 'react-native';

import { Palette, resolveColors } from '@/constants/theme';
import { captureException, initSentry, wrapRoot } from '@/services/sentry';
import { hydrate, useAppState } from '@/state/store';

// Crash reporting first, so errors during startup are captured too.
initSentry();
SplashScreen.preventAutoHideAsync().catch(() => {});

function RootLayout() {
  const scheme = useColorScheme();
  const hydrated = useAppState((s) => s.hydrated);
  const themeName = useAppState((s) => s.theme);
  const palette = resolveColors(scheme, themeName);

  useEffect(() => {
    hydrate()
      .catch((error) => captureException(error))
      .finally(() => SplashScreen.hideAsync().catch(() => {}));
  }, []);

  if (!hydrated) return null;

  const base = scheme === 'dark' ? DarkTheme : DefaultTheme;
  const theme = {
    ...base,
    colors: { ...base.colors, primary: palette.accent, background: palette.background, card: palette.surface, text: palette.text, border: palette.border },
  };

  return (
    <ThemeProvider value={theme}>
      <StatusBar style="auto" />
      <Stack screenOptions={{ headerTintColor: palette.accent, headerTitleStyle: { color: palette.text } }}>
        <Stack.Screen name="(tabs)" options={{ headerShown: false }} />
        <Stack.Screen name="chat/[id]" options={{ title: '' }} />
        <Stack.Screen name="new-chat" options={{ title: 'New chat', presentation: 'modal' }} />
      </Stack>
    </ThemeProvider>
  );
}

/**
 * A render error in any screen lands here instead of tearing the app down. Without it, React Native
 * (bridgeless, SDK 58) destroys the JS runtime before Sentry can send the crash, so it is never reported.
 */
export function ErrorBoundary({ error, retry }: ErrorBoundaryProps) {
  useEffect(() => {
    captureException(error);
  }, [error]);
  return (
    <View testID="error-screen" style={{ flex: 1, justifyContent: 'center', padding: 24, gap: 12, backgroundColor: Palette.light.background }}>
      <Text style={{ fontSize: 22, fontWeight: '700', color: Palette.light.text }}>Something went wrong</Text>
      <Text testID="error-message" style={{ color: Palette.light.text }}>{error.message}</Text>
      <Pressable testID="error-retry" onPress={retry} style={{ padding: 14, borderRadius: 10, backgroundColor: Palette.light.accent, alignItems: 'center' }}>
        <Text style={{ color: '#fff', fontWeight: '600' }}>Try again</Text>
      </Pressable>
    </View>
  );
}

export default wrapRoot(RootLayout);
