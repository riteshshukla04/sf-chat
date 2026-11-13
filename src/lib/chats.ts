/**
 * Chat logic: plain TypeScript, no React, so it is easy to test (src/lib/__tests__/).
 * Screens and the store call these; nothing here touches storage or the network.
 */

export type MessageStatus = 'sending' | 'sent' | 'delivered' | 'read';

export interface Message {
  id: string;
  /** "me" for my own messages, otherwise a contact id. */
  from: string;
  text: string;
  /** Epoch milliseconds. */
  sentAt: number;
  status: MessageStatus;
  /** Who reacted with which emoji: emoji → ids of the people ("me" or contact ids). Absent on old messages. */
  reactions?: Record<string, string[]>;
}

export const REACTION_EMOJIS = ['👍', '❤️', '😂', '😮', '😢', '🙏'] as const;

export interface Contact {
  id: string;
  name: string;
  about: string;
  /** Epoch milliseconds the contact was last online, or null when online now. */
  lastSeenAt: number | null;
}

export interface Chat {
  id: string;
  kind: 'direct' | 'group';
  title: string;
  /** Contact ids (without "me"). */
  members: string[];
  /** Oldest first. */
  messages: Message[];
  /** How many of the newest messages I haven't read. */
  unread: number;
  pinned: boolean;
  muted: boolean;
  createdAt: number;
}

const MINUTE = 60_000;
const DAY = 24 * 60 * MINUTE;
const WEEKDAYS = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

const pad = (n: number) => String(n).padStart(2, '0');

/** "14:05" today, "Yesterday", the weekday within a week, else "12/11/2026". */
export function formatTime(at: number, now: number): string {
  const time = new Date(at);
  const today = new Date(now);
  const startOfToday = new Date(today.getFullYear(), today.getMonth(), today.getDate()).getTime();
  if (at >= startOfToday) return `${pad(time.getHours())}:${pad(time.getMinutes())}`;
  if (at >= startOfToday - DAY) return 'Yesterday';
  if (at >= startOfToday - 6 * DAY) return WEEKDAYS[time.getDay()]!;
  return `${pad(time.getDate())}/${pad(time.getMonth() + 1)}/${time.getFullYear()}`;
}

/** Clock time of a message inside a conversation. */
export function formatClock(at: number): string {
  const time = new Date(at);
  return `${pad(time.getHours())}:${pad(time.getMinutes())}`;
}

/** When something last happened in a chat: its newest message, or when it was created. */
export function lastActivityAt(chat: Chat): number {
  return chat.messages.length ? chat.messages[chat.messages.length - 1]!.sentAt : chat.createdAt;
}

/** Pinned chats first, then the most recently active. */
export function sortChats(chats: Chat[]): Chat[] {
  return [...chats].sort((a, b) => Number(b.pinned) - Number(a.pinned) || lastActivityAt(b) - lastActivityAt(a));
}

/** The line under a chat's name in the list. */
export function previewText(chat: Chat, contacts: Record<string, Contact>): string {
  const last = chat.messages[chat.messages.length - 1];
  if (!last) return chat.kind === 'group' ? 'Group created. Say hi!' : 'No messages yet';
  if (last.from === 'me') return `You: ${last.text}`;
  if (chat.kind === 'group') return `${firstName(contacts[last.from]?.name ?? 'Someone')}: ${last.text}`;
  return last.text;
}

export function firstName(name: string): string {
  return name.split(' ')[0] ?? name;
}

/** Total unread messages over all chats that aren't muted (the badge on the Chats tab). */
export function totalUnread(chats: Chat[]): number {
  return chats.filter((c) => !c.muted).reduce((sum, c) => sum + c.unread, 0);
}

/** Chats whose title, or any message, contains the query (case-insensitive). */
export function searchChats(chats: Chat[], query: string): Chat[] {
  const q = query.trim().toLowerCase();
  if (!q) return chats;
  return chats.filter((c) => c.title.toLowerCase().includes(q) || c.messages.some((m) => m.text.toLowerCase().includes(q)));
}

/** The line under the name in a conversation's header. */
export function headerSubtitle(chat: Chat, contacts: Record<string, Contact>, now: number): string {
  if (chat.kind === 'group') {
    const names = chat.members.map((id) => firstName(contacts[id]?.name ?? 'Someone'));
    // For a group: who is in it, and when the last message came in.
    const last = chat.messages[chat.messages.length - 1];
    const members = `${names.join(', ')}, You`;
    // A brand-new group has no messages yet.
    return last ? `${members} · last message ${formatTime(last.sentAt, now)}` : members;
  }
  const contact = contacts[chat.members[0]!];
  if (!contact) return '';
  if (contact.lastSeenAt === null) return 'online';
  if (now - contact.lastSeenAt < MINUTE) return 'last seen just now';
  const label = formatTime(contact.lastSeenAt, now);
  return label.includes(':') ? `last seen today at ${label}` : `last seen ${label.toLowerCase()}`;
}

/** A new message of mine, not yet sent. */
export function draftMessage(text: string, now: number, id: string): Message {
  return { id, from: 'me', text: text.trim(), sentAt: now, status: 'sending' };
}

/** Whether a draft can be sent: something other than spaces, at most 4,000 characters. */
export function canSend(text: string): boolean {
  const trimmed = text.trim();
  return trimmed.length > 0 && trimmed.length <= 4000;
}

/** The chat with one more message, and my unread count back to zero (I'm looking at it). */
export function withMessage(chat: Chat, message: Message): Chat {
  return { ...chat, messages: [...chat.messages, message], unread: 0 };
}

/** Moves my message one step on: sending → sent → delivered → read. */
export function advanceStatus(status: MessageStatus): MessageStatus {
  return status === 'sending' ? 'sent' : status === 'sent' ? 'delivered' : 'read';
}

/** Messages grouped under day headers ("Today", "Yesterday", ...), oldest day first. */
export function groupByDay(messages: Message[], now: number): Array<{ day: string; messages: Message[] }> {
  const groups: Array<{ day: string; messages: Message[] }> = [];
  for (const message of messages) {
    const label = dayLabel(message.sentAt, now);
    const current = groups[groups.length - 1];
    if (current && current.day === label) current.messages.push(message);
    else groups.push({ day: label, messages: [message] });
  }
  return groups;
}

export function dayLabel(at: number, now: number): string {
  const label = formatTime(at, now);
  return label.includes(':') ? 'Today' : label;
}

/** Initials for an avatar: "Asha Rao" → "AR", "Weekend trip" → "WT". */
export function initials(name: string): string {
  const letters = name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0]!.toUpperCase());
  return letters.join('') || '?';
}

/**
 * Toggles a person's reaction on a message. Reacting with the emoji they already used removes it;
 * a different emoji replaces their earlier one (one reaction per person). Unknown emojis are ignored.
 */
export function toggleReaction(message: Message, emoji: string, by = 'me'): Message {
  if (!(REACTION_EMOJIS as readonly string[]).includes(emoji)) return message;
  const next: Record<string, string[]> = {};
  const had = message.reactions?.[emoji]?.includes(by) ?? false;
  for (const [e, people] of Object.entries(message.reactions ?? {})) {
    const rest = people.filter((p) => p !== by);
    if (rest.length) next[e] = rest;
  }
  if (!had) next[emoji] = [...(next[emoji] ?? []), by];
  const { reactions: _old, ...base } = message;
  return Object.keys(next).length ? { ...base, reactions: next } : base;
}

/** Reactions as pills, in the picker's order: emoji, count, and whether I used it. */
export function reactionSummary(message: Message): Array<{ emoji: string; count: number; mine: boolean }> {
  const out: Array<{ emoji: string; count: number; mine: boolean }> = [];
  for (const emoji of REACTION_EMOJIS) {
    const people = message.reactions?.[emoji];
    if (people?.length) out.push({ emoji, count: people.length, mine: people.includes('me') });
  }
  return out;
}
