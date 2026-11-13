import { MaterialCommunityIcons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Avatar } from '@/components/avatar';
import { Spacing, useColors } from '@/constants/theme';
import { formatTime, lastActivityAt, previewText, searchChats, sortChats, type Chat } from '@/lib/chats';
import { togglePin, useAppState } from '@/state/store';

/** What's new in this update, set when it is published (EXPO_PUBLIC_RELEASE_NOTE, see scripts/publish-update.sh). */
const RELEASE_NOTE = process.env.EXPO_PUBLIC_RELEASE_NOTE;

export default function ChatsScreen() {
  const c = useColors();
  const chats = useAppState((s) => s.chats);
  const contacts = useAppState((s) => s.contacts);
  const [query, setQuery] = useState('');
  const shown = useMemo(() => sortChats(searchChats(chats, query)), [chats, query]);
  const now = Date.now();

  const row = ({ item }: { item: Chat }) => {
    const last = item.messages[item.messages.length - 1];
    return (
      <Pressable
        testID={`chat-row-${item.id}`}
        onPress={() => router.push(`/chat/${item.id}`)}
        onLongPress={() => togglePin(item.id)}
        style={({ pressed }) => [styles.row, { backgroundColor: pressed ? c.surfaceAlt : c.surface }]}>
        <Avatar name={item.title} group={item.kind === 'group'} />
        <View style={styles.rowBody}>
          <View style={styles.rowTop}>
            <Text style={[styles.name, { color: c.text }]} numberOfLines={1}>
              {item.title}
            </Text>
            <Text style={[styles.time, { color: item.unread && !item.muted ? c.accent : c.muted }]}>{formatTime(lastActivityAt(item), now)}</Text>
          </View>
          <View style={styles.rowTop}>
            <Text style={[styles.preview, { color: c.muted }]} numberOfLines={1}>
              {last?.from === 'me' ? (last.status === 'read' ? '✓✓ ' : last.status === 'sending' ? '🕓 ' : '✓ ') : ''}
              {previewText(item, contacts)}
            </Text>
            <View style={styles.badges}>
              {item.muted ? <MaterialCommunityIcons name="bell-off-outline" size={16} color={c.muted} /> : null}
              {item.pinned ? <MaterialCommunityIcons name="pin-outline" size={16} color={c.muted} /> : null}
              {item.unread ? (
                <View style={[styles.unread, { backgroundColor: item.muted ? c.muted : c.accent }]}>
                  <Text style={[styles.unreadText, { color: c.onAccent }]}>{item.unread}</Text>
                </View>
              ) : null}
            </View>
          </View>
        </View>
      </Pressable>
    );
  };

  return (
    <SafeAreaView edges={['top']} style={[styles.flex, { backgroundColor: c.surface }]} testID="chats-screen">
      <View style={styles.header}>
        <Text style={[styles.title, { color: c.accent }]} accessibilityRole="header">
          Chatter
        </Text>
        {RELEASE_NOTE ? (
          <Text testID="release-note" style={[styles.note, { color: c.muted }]}>
            {RELEASE_NOTE}
          </Text>
        ) : null}
      </View>
      <View style={[styles.search, { backgroundColor: c.surfaceAlt }]}>
        <MaterialCommunityIcons name="magnify" size={20} color={c.muted} />
        <TextInput
          testID="chat-search"
          value={query}
          onChangeText={setQuery}
          placeholder="Search"
          placeholderTextColor={c.muted}
          style={[styles.searchInput, { color: c.text }]}
        />
      </View>
      <FlatList
        data={shown}
        keyExtractor={(item) => item.id}
        renderItem={row}
        ListEmptyComponent={<Text style={[styles.empty, { color: c.muted }]}>No chats match “{query}”</Text>}
      />
      <Pressable testID="new-chat" onPress={() => router.push('/new-chat')} style={[styles.fab, { backgroundColor: c.accent }]}>
        <MaterialCommunityIcons name="message-plus-outline" size={26} color={c.onAccent} />
      </Pressable>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  header: { paddingHorizontal: Spacing.lg, paddingTop: Spacing.sm, paddingBottom: Spacing.sm },
  title: { fontSize: 26, fontWeight: '700' },
  note: { fontSize: 13, marginTop: 2 },
  search: { flexDirection: 'row', alignItems: 'center', marginHorizontal: Spacing.lg, marginBottom: Spacing.sm, borderRadius: 22, paddingHorizontal: Spacing.md, gap: Spacing.sm },
  searchInput: { flex: 1, paddingVertical: 10, fontSize: 16 },
  row: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: Spacing.lg, paddingVertical: Spacing.md, gap: Spacing.md },
  rowBody: { flex: 1, gap: 4 },
  rowTop: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: Spacing.sm },
  name: { flex: 1, fontSize: 17, fontWeight: '600' },
  time: { fontSize: 12 },
  preview: { flex: 1, fontSize: 15 },
  badges: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  unread: { minWidth: 22, height: 22, borderRadius: 11, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 6 },
  unreadText: { fontSize: 12, fontWeight: '700' },
  empty: { textAlign: 'center', marginTop: Spacing.xl },
  fab: { position: 'absolute', right: Spacing.lg, bottom: Spacing.lg, width: 58, height: 58, borderRadius: 18, alignItems: 'center', justifyContent: 'center', elevation: 4 },
});
