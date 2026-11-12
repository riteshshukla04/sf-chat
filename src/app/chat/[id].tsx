import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Stack, useLocalSearchParams } from 'expo-router';
import { useEffect, useMemo, useState } from 'react';
import { FlatList, KeyboardAvoidingView, Platform, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Avatar } from '@/components/avatar';
import { Spacing, useColors } from '@/constants/theme';
import { canSend, firstName, formatClock, groupByDay, headerSubtitle, type Message, REACTION_EMOJIS, reactionSummary } from '@/lib/chats';
import { addBreadcrumb } from '@/services/sentry';
import { markRead, reactToMessage, sendMessage, useAppState } from '@/state/store';

type Row = { kind: 'day'; key: string; day: string } | { kind: 'message'; key: string; message: Message };

export default function ChatScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const c = useColors();
  const chat = useAppState((s) => s.chats.find((x) => x.id === id));
  const contacts = useAppState((s) => s.contacts);
  const [draft, setDraft] = useState('');
  const [picking, setPicking] = useState<string | null>(null); // id of the message whose picker is open

  useEffect(() => {
    addBreadcrumb('open chat', { chat: id });
    if (id) markRead(id);
  }, [id]);

  const rows = useMemo<Row[]>(() => {
    if (!chat) return [];
    const out: Row[] = [];
    for (const group of groupByDay(chat.messages, Date.now())) {
      out.push({ kind: 'day', key: `day-${group.day}`, day: group.day });
      for (const message of group.messages) out.push({ kind: 'message', key: message.id, message });
    }
    return out.reverse(); // the list is inverted: newest at the bottom
  }, [chat]);

  if (!chat) {
    return (
      <View style={[styles.center, { backgroundColor: c.background }]}>
        <Text style={{ color: c.muted }}>This chat doesn&apos;t exist any more.</Text>
      </View>
    );
  }

  const subtitle = headerSubtitle(chat, contacts, Date.now());
  const send = () => {
    if (!canSend(draft)) return;
    sendMessage(chat.id, draft);
    setDraft('');
  };

  const renderRow = ({ item }: { item: Row }) => {
    if (item.kind === 'day') {
      return (
        <View style={[styles.day, { backgroundColor: c.surfaceAlt }]}>
          <Text style={[styles.dayText, { color: c.muted }]}>{item.day}</Text>
        </View>
      );
    }
    const m = item.message;
    const mine = m.from === 'me';
    const pills = reactionSummary(m);
    return (
      <View style={[styles.item, mine ? styles.mine : styles.theirs]}>
      {picking === m.id ? (
        <View testID="reaction-picker" style={[styles.picker, { backgroundColor: c.surface }]}>
          {REACTION_EMOJIS.map((emoji) => (
            <Pressable
              key={emoji}
              testID={`pick-${emoji}`}
              accessibilityLabel={`React ${emoji}`}
              onPress={() => {
                reactToMessage(chat.id, m.id, emoji);
                setPicking(null);
              }}
              style={styles.pickEmoji}>
              <Text style={styles.pickText}>{emoji}</Text>
            </Pressable>
          ))}
        </View>
      ) : null}
      <Pressable
        testID={`message-${m.id}`}
        delayLongPress={350}
        onLongPress={() => setPicking(picking === m.id ? null : m.id)}
        style={[styles.bubble, { backgroundColor: mine ? c.bubbleMine : c.bubbleTheirs }]}>
        {chat.kind === 'group' && !mine ? <Text style={[styles.sender, { color: c.accent }]}>{firstName(contacts[m.from]?.name ?? 'Someone')}</Text> : null}
        <Text style={[styles.text, { color: c.text }]}>{m.text}</Text>
        <View style={styles.meta}>
          <Text style={[styles.clock, { color: c.muted }]}>{formatClock(m.sentAt)}</Text>
          {mine ? (
            <MaterialCommunityIcons
              testID={`ticks-${m.status}`}
              name={m.status === 'sending' ? 'clock-outline' : m.status === 'sent' ? 'check' : 'check-all'}
              size={15}
              color={m.status === 'read' ? c.tickRead : c.muted}
            />
          ) : null}
        </View>
      </Pressable>
      {pills.length ? (
        <View style={styles.pills}>
          {pills.map((p) => (
            <Pressable
              key={p.emoji}
              testID={`reaction-${m.id}-${p.emoji}`}
              onPress={() => reactToMessage(chat.id, m.id, p.emoji)}
              style={[styles.pill, { backgroundColor: p.mine ? c.bubbleMine : c.surface, borderColor: p.mine ? c.accent : c.surfaceAlt }]}>
              <Text style={styles.pillText}>
                {p.emoji}
                {p.count > 1 ? ` ${p.count}` : ''}
              </Text>
            </Pressable>
          ))}
        </View>
      ) : null}
      </View>
    );
  };

  return (
    <SafeAreaView edges={['bottom']} style={[styles.flex, { backgroundColor: c.chatBackground }]} testID="chat-screen">
      <Stack.Screen
        options={{
          headerTitle: () => (
            <View style={styles.headerTitle}>
              <Avatar name={chat.title} size={36} group={chat.kind === 'group'} />
              <View style={{ flexShrink: 1 }}>
                <Text style={[styles.headerName, { color: c.text }]} numberOfLines={1}>
                  {chat.title}
                </Text>
                <Text testID="chat-subtitle" style={[styles.headerSub, { color: c.muted }]} numberOfLines={1}>
                  {subtitle}
                </Text>
              </View>
            </View>
          ),
        }}
      />
      <KeyboardAvoidingView style={styles.flex} behavior={Platform.OS === 'ios' ? 'padding' : undefined} keyboardVerticalOffset={90}>
        <FlatList
          inverted
          data={rows}
          keyExtractor={(row) => row.key}
          renderItem={renderRow}
          extraData={picking}
          contentContainerStyle={styles.list}
          ListFooterComponent={
            chat.messages.length === 0 ? (
              <View style={[styles.notice, { backgroundColor: c.surfaceAlt }]}>
                <Text style={{ color: c.muted, textAlign: 'center' }}>Messages are kept on this phone. Say hi 👋</Text>
              </View>
            ) : undefined
          }
        />
        <View style={styles.inputBar}>
          <TextInput
            testID="message-input"
            value={draft}
            onChangeText={setDraft}
            placeholder="Message"
            placeholderTextColor={c.muted}
            multiline
            style={[styles.input, { backgroundColor: c.surface, color: c.text }]}
          />
          <Pressable testID="send" onPress={send} disabled={!canSend(draft)} style={[styles.send, { backgroundColor: c.accent, opacity: canSend(draft) ? 1 : 0.5 }]}>
            <MaterialCommunityIcons name="send" size={22} color={c.onAccent} />
          </Pressable>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  flex: { flex: 1 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  headerTitle: { flexDirection: 'row', alignItems: 'center', gap: Spacing.sm, marginLeft: -Spacing.sm },
  headerName: { fontSize: 17, fontWeight: '600' },
  headerSub: { fontSize: 12 },
  list: { padding: Spacing.md, gap: 6 },
  day: { alignSelf: 'center', borderRadius: 8, paddingHorizontal: Spacing.md, paddingVertical: 4, marginVertical: Spacing.sm },
  dayText: { fontSize: 12, fontWeight: '600' },
  item: { maxWidth: '82%' },
  picker: { flexDirection: 'row', borderRadius: 22, paddingHorizontal: 6, marginBottom: 4, elevation: 3 },
  pickEmoji: { padding: 6 },
  pickText: { fontSize: 24 },
  pills: { flexDirection: 'row', gap: 4, marginTop: -6, marginHorizontal: 6 },
  pill: { borderRadius: 12, borderWidth: 1, paddingHorizontal: 7, paddingVertical: 2 },
  pillText: { fontSize: 13 },
  bubble: { alignSelf: 'stretch', borderRadius: 12, paddingHorizontal: Spacing.md, paddingTop: 6, paddingBottom: 4 },
  mine: { alignSelf: 'flex-end', borderTopRightRadius: 2 },
  theirs: { alignSelf: 'flex-start', borderTopLeftRadius: 2 },
  sender: { fontSize: 13, fontWeight: '700', marginBottom: 2 },
  text: { fontSize: 16, lineHeight: 21 },
  meta: { flexDirection: 'row', alignSelf: 'flex-end', alignItems: 'center', gap: 4, marginTop: 2 },
  clock: { fontSize: 11 },
  notice: { alignSelf: 'center', borderRadius: 8, padding: Spacing.md, margin: Spacing.lg },
  inputBar: { flexDirection: 'row', alignItems: 'flex-end', padding: Spacing.sm, gap: Spacing.sm },
  input: { flex: 1, borderRadius: 22, paddingHorizontal: Spacing.lg, paddingVertical: 10, fontSize: 16, maxHeight: 120 },
  send: { width: 46, height: 46, borderRadius: 23, alignItems: 'center', justifyContent: 'center' },
});
