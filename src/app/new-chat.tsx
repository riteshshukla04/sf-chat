import { router } from 'expo-router';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';

import { Avatar } from '@/components/avatar';
import { Spacing, useColors } from '@/constants/theme';
import { startChat, useAppState } from '@/state/store';

export default function NewChatScreen() {
  const c = useColors();
  const contacts = useAppState((s) => Object.values(s.contacts).sort((a, b) => a.name.localeCompare(b.name)));

  return (
    <FlatList
      testID="new-chat-screen"
      style={{ backgroundColor: c.surface }}
      data={contacts}
      keyExtractor={(contact) => contact.id}
      ListHeaderComponent={<Text style={[styles.section, { color: c.muted }]}>Contacts on Chatter</Text>}
      renderItem={({ item }) => (
        <Pressable
          testID={`contact-${item.id}`}
          onPress={() => router.replace(`/chat/${startChat(item.id)}`)}
          style={({ pressed }) => [styles.row, { backgroundColor: pressed ? c.surfaceAlt : c.surface }]}>
          <Avatar name={item.name} size={44} />
          <View style={{ flex: 1 }}>
            <Text style={[styles.name, { color: c.text }]}>{item.name}</Text>
            <Text style={[styles.about, { color: c.muted }]} numberOfLines={1}>
              {item.about}
            </Text>
          </View>
        </Pressable>
      )}
    />
  );
}

const styles = StyleSheet.create({
  section: { paddingHorizontal: Spacing.lg, paddingTop: Spacing.lg, paddingBottom: Spacing.sm, fontSize: 13, fontWeight: '600' },
  row: { flexDirection: 'row', alignItems: 'center', gap: Spacing.md, paddingHorizontal: Spacing.lg, paddingVertical: Spacing.md },
  name: { fontSize: 16, fontWeight: '600' },
  about: { fontSize: 14 },
});
