import { MaterialCommunityIcons } from '@expo/vector-icons';
import { FlatList, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Avatar } from '@/components/avatar';
import { Spacing, useColors } from '@/constants/theme';
import { formatTime } from '@/lib/chats';
import { useAppState } from '@/state/store';

export default function CallsScreen() {
  const c = useColors();
  const calls = useAppState((s) => s.calls);
  const contacts = useAppState((s) => s.contacts);
  const now = Date.now();

  return (
    <SafeAreaView edges={['top']} style={[styles.flex, { backgroundColor: c.surface }]} testID="calls-screen">
      <Text style={[styles.title, { color: c.text }]} accessibilityRole="header">
        Calls
      </Text>
      <FlatList
        data={calls}
        keyExtractor={(call) => call.id}
        renderItem={({ item }) => {
          const name = contacts[item.contactId]?.name ?? 'Unknown';
          const missed = item.direction === 'missed';
          return (
            <View style={styles.row}>
              <Avatar name={name} size={44} />
              <View style={{ flex: 1 }}>
                <Text style={[styles.name, { color: missed ? c.danger : c.text }]}>{name}</Text>
                <View style={styles.detail}>
                  <MaterialCommunityIcons
                    name={item.direction === 'outgoing' ? 'call-made' : item.direction === 'incoming' ? 'call-received' : 'call-missed'}
                    size={15}
                    color={missed ? c.danger : c.accent}
                  />
                  <Text style={{ color: c.muted }}>{formatTime(item.at, now)}</Text>
                </View>
              </View>
              <MaterialCommunityIcons name={item.video ? 'video-outline' : 'phone-outline'} size={22} color={c.accent} />
            </View>
          );
        }}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  title: { fontSize: 26, fontWeight: '700', paddingHorizontal: Spacing.lg, paddingVertical: Spacing.sm },
  row: { flexDirection: 'row', alignItems: 'center', gap: Spacing.md, paddingHorizontal: Spacing.lg, paddingVertical: Spacing.md },
  name: { fontSize: 16, fontWeight: '600' },
  detail: { flexDirection: 'row', alignItems: 'center', gap: 4 },
});
