import { StyleSheet, Text, View } from 'react-native';

import { initials } from '@/lib/chats';

// A stable colour per name, so the same person always gets the same avatar.
const COLOURS = ['#25A36F', '#3E8EDE', '#D9822B', '#9B59B6', '#E0566E', '#16A2B8', '#7A8B2E', '#C0612B'];

function colourFor(name: string): string {
  let hash = 0;
  for (const ch of name) hash = (hash * 31 + ch.charCodeAt(0)) >>> 0;
  return COLOURS[hash % COLOURS.length]!;
}

export function Avatar({ name, size = 48, group = false }: { name: string; size?: number; group?: boolean }) {
  return (
    <View style={[styles.circle, { width: size, height: size, borderRadius: size / 2, backgroundColor: colourFor(name) }]}>
      <Text style={[styles.text, { fontSize: size * (group ? 0.34 : 0.38) }]}>{initials(name)}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  circle: { alignItems: 'center', justifyContent: 'center' },
  text: { color: '#fff', fontWeight: '700' },
});
