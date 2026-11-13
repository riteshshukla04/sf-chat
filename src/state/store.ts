/**
 * App state: contacts, chats and calls, persisted to AsyncStorage on the device.
 *
 * A tiny external store (no library). Screens read it with useAppState(selector);
 * actions below update it, then persist. The chat logic itself lives in src/lib/chats.ts.
 * There is no server: replies to your messages are simulated on the device.
 */
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useSyncExternalStore } from 'react';

import { buildSeed, type Call } from '@/data/seed';
import { advanceStatus, type Chat, type Contact, draftMessage, toggleReaction, withMessage } from '@/lib/chats';

export const STORAGE_KEY = 'chatter:v1';

export interface Profile {
  name: string;
  about: string;
}

export interface AppState {
  hydrated: boolean;
  profile: Profile;
  contacts: Record<string, Contact>;
  chats: Chat[];
  calls: Call[];
}

let state: AppState = { hydrated: false, profile: { name: 'You', about: 'Available' }, contacts: {}, chats: [], calls: [] };
const listeners = new Set<() => void>();

function set(next: Partial<AppState>, persist = true): void {
  state = { ...state, ...next };
  for (const listener of listeners) listener();
  if (persist && state.hydrated) void save();
}

async function save(): Promise<void> {
  const { profile, contacts, chats, calls } = state;
  await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify({ profile, contacts, chats, calls }));
}

export function getState(): AppState {
  return state;
}

function subscribe(listener: () => void): () => void {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function useAppState<T>(selector: (s: AppState) => T): T {
  return useSyncExternalStore(subscribe, () => selector(state), () => selector(state));
}

/** Loads what was saved, or the sample conversations on a first launch. */
export async function hydrate(now = Date.now()): Promise<void> {
  const raw = await AsyncStorage.getItem(STORAGE_KEY);
  const saved = raw ? (JSON.parse(raw) as Omit<AppState, 'hydrated'>) : null;
  const seed = saved ?? { profile: state.profile, ...buildSeed(now) };
  set({ ...seed, hydrated: true }, !saved);
}

export function chatById(id: string): Chat | undefined {
  return state.chats.find((c) => c.id === id);
}

function replaceChat(chat: Chat): void {
  set({ chats: state.chats.map((c) => (c.id === chat.id ? chat : c)) });
}

export function markRead(chatId: string): void {
  const chat = chatById(chatId);
  if (chat && chat.unread > 0) replaceChat({ ...chat, unread: 0 });
}

let counter = 0;
const newId = (prefix: string) => `${prefix}-${Date.now()}-${++counter}`;

const REPLIES = ['👍', 'Sounds good!', 'Haha 😄', 'On it', 'Talk later?', 'Nice!', 'Sure thing'];

/** Sends my message; its ticks move on, and in a direct chat the other person may answer. */
export function sendMessage(chatId: string, text: string, now = Date.now()): void {
  const chat = chatById(chatId);
  if (!chat) return;
  const message = draftMessage(text, now, newId('m'));
  replaceChat(withMessage(chat, message));
  const step = () => {
    const current = chatById(chatId);
    if (!current) return;
    replaceChat({
      ...current,
      messages: current.messages.map((m) => (m.id === message.id ? { ...m, status: advanceStatus(m.status) } : m)),
    });
  };
  setTimeout(step, 400);
  setTimeout(step, 1200);
  setTimeout(step, 2600);
  if (chat.kind === 'direct') {
    setTimeout(() => {
      const current = chatById(chatId);
      if (!current) return;
      const reply = { id: newId('r'), from: chat.members[0]!, text: REPLIES[current.messages.length % REPLIES.length]!, sentAt: Date.now(), status: 'read' as const };
      replaceChat({ ...current, messages: [...current.messages, reply] });
    }, 4200);
  }
}

/** Opens the direct chat with a contact, starting one if there is none yet. Returns its id. */
export function startChat(contactId: string, now = Date.now()): string {
  const existing = state.chats.find((c) => c.kind === 'direct' && c.members[0] === contactId);
  if (existing) return existing.id;
  const contact = state.contacts[contactId];
  const chat: Chat = { id: newId('chat'), kind: 'direct', title: contact?.name ?? 'Chat', members: [contactId], messages: [], unread: 0, pinned: false, muted: false, createdAt: now };
  set({ chats: [chat, ...state.chats] });
  return chat.id;
}

/** Adds, switches or removes my reaction on a message; saved with the chat. */
export function reactToMessage(chatId: string, messageId: string, emoji: string): void {
  const chat = chatById(chatId);
  if (!chat) return;
  replaceChat({ ...chat, messages: chat.messages.map((m) => (m.id === messageId ? toggleReaction(m, emoji) : m)) });
}

export function togglePin(chatId: string): void {
  const chat = chatById(chatId);
  if (chat) replaceChat({ ...chat, pinned: !chat.pinned });
}

export function updateProfile(profile: Partial<Profile>): void {
  set({ profile: { ...state.profile, ...profile } });
}

/** Back to the sample conversations (Settings → Reset chats). */
export async function resetChats(now = Date.now()): Promise<void> {
  await AsyncStorage.removeItem(STORAGE_KEY);
  set({ ...buildSeed(now), hydrated: true });
}
