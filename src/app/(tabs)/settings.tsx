import { Alert, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Avatar } from '@/components/avatar';
import { Spacing, THEME_LABELS, THEME_NAMES, useColors } from '@/constants/theme';
import { getBuildInfo } from '@/services/build-info';
import { sentryEnabled } from '@/services/sentry';
import { resetChats, setTheme, useAppState } from '@/state/store';

function Row({ label, value, testID }: { label: string; value: string; testID?: string }) {
  const c = useColors();
  return (
    <View style={[styles.row, { borderBottomColor: c.border }]} testID={testID}>
      <Text style={[styles.label, { color: c.text }]}>{label}</Text>
      <Text style={[styles.value, { color: c.muted }]} numberOfLines={1}>
        {value}
      </Text>
    </View>
  );
}

export default function SettingsScreen() {
  const c = useColors();
  const profile = useAppState((s) => s.profile);
  const theme = useAppState((s) => s.theme);
  const info = getBuildInfo();

  return (
    <SafeAreaView edges={['top']} style={[styles.flex, { backgroundColor: c.surface }]} testID="settings-screen">
      <ScrollView>
        <Text style={[styles.title, { color: c.text }]} accessibilityRole="header">
          Settings
        </Text>
        <View style={[styles.profile, { borderBottomColor: c.border }]}>
          <Avatar name={profile.name} size={64} />
          <View>
            <Text style={[styles.profileName, { color: c.text }]}>{profile.name}</Text>
            <Text style={{ color: c.muted }}>{profile.about}</Text>
          </View>
        </View>
        <View style={[styles.themeBox, { borderBottomColor: c.border }]} testID="theme-picker">
          <Text style={[styles.label, { color: c.text }]}>Theme</Text>
          <View style={styles.themeOptions}>
            {THEME_NAMES.map((name) => {
              const selected = name === theme;
              return (
                <Pressable
                  key={name}
                  testID={`theme-${name}`}
                  accessibilityRole="radio"
                  accessibilityState={{ selected }}
                  onPress={() => setTheme(name)}
                  style={[styles.themeChip, { borderColor: selected ? c.accent : c.border, backgroundColor: selected ? c.accent : 'transparent' }]}>
                  <Text style={{ color: selected ? c.onAccent : c.text, fontWeight: '600' }}>{THEME_LABELS[name]}</Text>
                </Pressable>
              );
            })}
          </View>
        </View>
        <Row label="Account" value="Privacy, security" />
        <Row label="Notifications" value="Message and call tones" />
        <Row label="Storage" value="Messages are kept on this phone" />
        <Row label="Version" value={`${info.appVersion} (${info.updateLabel === 'embedded' ? 'built in' : info.updateLabel.slice(0, 8)})`} testID="version-row" />
        <Row label="Update channel" value={info.channel} />
        <Row label="Crash reporting" value={sentryEnabled ? 'Sentry on' : 'off (no DSN)'} testID="crash-reporting-row" />
        <Pressable
          testID="reset-chats"
          onPress={() =>
            Alert.alert('Reset chats?', 'Back to the sample conversations.', [
              { text: 'Cancel', style: 'cancel' },
              { text: 'Reset', style: 'destructive', onPress: () => void resetChats() },
            ])
          }
          style={styles.reset}>
          <Text style={{ color: c.danger, fontWeight: '600' }}>Reset chats</Text>
        </Pressable>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  title: { fontSize: 26, fontWeight: '700', paddingHorizontal: Spacing.lg, paddingVertical: Spacing.sm },
  profile: { flexDirection: 'row', alignItems: 'center', gap: Spacing.lg, padding: Spacing.lg, borderBottomWidth: StyleSheet.hairlineWidth },
  profileName: { fontSize: 20, fontWeight: '600' },
  row: { flexDirection: 'row', justifyContent: 'space-between', gap: Spacing.lg, paddingHorizontal: Spacing.lg, paddingVertical: Spacing.md, borderBottomWidth: StyleSheet.hairlineWidth },
  label: { fontSize: 16 },
  value: { fontSize: 14, flexShrink: 1, textAlign: 'right' },
  themeBox: { gap: Spacing.sm, paddingHorizontal: Spacing.lg, paddingVertical: Spacing.md, borderBottomWidth: StyleSheet.hairlineWidth },
  themeOptions: { flexDirection: 'row', gap: Spacing.sm },
  themeChip: { paddingHorizontal: Spacing.lg, paddingVertical: Spacing.sm, borderRadius: 20, borderWidth: 1 },
  reset: { padding: Spacing.lg },
});
