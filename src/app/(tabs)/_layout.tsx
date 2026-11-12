import { MaterialCommunityIcons } from '@expo/vector-icons';
import { Tabs } from 'expo-router/js-tabs';
import type { ComponentProps } from 'react';
import type { ColorValue } from 'react-native';

import { useColors } from '@/constants/theme';
import { totalUnread } from '@/lib/chats';
import { useAppState } from '@/state/store';

type IconName = ComponentProps<typeof MaterialCommunityIcons>['name'];

function icon(name: IconName) {
  return function TabIcon({ color, size }: { color: ColorValue; size: number }) {
    return <MaterialCommunityIcons name={name} color={color as string} size={size} />;
  };
}

export default function TabsLayout() {
  const c = useColors();
  const unread = useAppState((s) => totalUnread(s.chats));

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: c.accent,
        tabBarInactiveTintColor: c.muted,
        tabBarStyle: { backgroundColor: c.surface, borderTopColor: c.border },
      }}>
      <Tabs.Screen
        name="index"
        options={{ title: 'Chats', tabBarIcon: icon('message-text-outline'), tabBarButtonTestID: 'tab-chats', tabBarBadge: unread || undefined }}
      />
      <Tabs.Screen name="calls" options={{ title: 'Calls', tabBarIcon: icon('phone-outline'), tabBarButtonTestID: 'tab-calls' }} />
      <Tabs.Screen name="settings" options={{ title: 'Settings', tabBarIcon: icon('cog-outline'), tabBarButtonTestID: 'tab-settings' }} />
    </Tabs>
  );
}
