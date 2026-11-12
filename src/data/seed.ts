/**
 * The conversations a fresh install starts with, timed relative to "now" so they always look recent.
 * Made-up people; nothing here comes from a real account.
 */
import type { Chat, Contact } from '@/lib/chats';

export interface Call {
  id: string;
  contactId: string;
  at: number;
  direction: 'incoming' | 'outgoing' | 'missed';
  video: boolean;
}

export interface Seed {
  contacts: Record<string, Contact>;
  chats: Chat[];
  calls: Call[];
}

const MIN = 60_000;
const HOUR = 60 * MIN;
const DAY = 24 * HOUR;

export function buildSeed(now: number): Seed {
  const contacts: Contact[] = [
    { id: 'asha', name: 'Asha Rao', about: 'Busy building things', lastSeenAt: null },
    { id: 'ben', name: 'Ben Okafor', about: 'At the gym', lastSeenAt: now - 25 * MIN },
    { id: 'chen', name: 'Chen Wei', about: 'Available', lastSeenAt: now - 3 * HOUR },
    { id: 'dana', name: 'Dana Silva', about: 'Hey there! I am using Chatter.', lastSeenAt: now - 2 * DAY },
    { id: 'eli', name: 'Eli Novak', about: 'Coffee first', lastSeenAt: null },
    { id: 'fatima', name: 'Fatima Khan', about: 'In a meeting', lastSeenAt: now - 50 * MIN },
    { id: 'gabe', name: 'Gabe Moreau', about: 'Travelling', lastSeenAt: now - 5 * DAY },
    { id: 'hana', name: 'Hana Ito', about: 'Urgent calls only', lastSeenAt: now - 9 * MIN },
  ];
  let n = 0;
  const m = (from: string, text: string, ago: number, status: 'sent' | 'delivered' | 'read' = 'read') => ({
    id: `seed-${++n}`,
    from,
    text,
    sentAt: now - ago,
    status,
  });

  const chats: Chat[] = [
    {
      id: 'family',
      kind: 'group',
      title: 'Family 🏠',
      members: ['chen', 'dana'],
      messages: [
        m('dana', 'Sunday lunch at ours?', 3 * HOUR),
        m('chen', 'Yes! I will bring dessert', 2 * HOUR + 40 * MIN),
        m('me', 'Count me in 🙌', 2 * HOUR + 30 * MIN),
        m('dana', 'Lunch at 1. Bring the photos from the trip', 50 * MIN),
      ],
      unread: 1,
      pinned: true,
      muted: false,
      createdAt: now - 40 * DAY,
    },
    {
      id: 'asha',
      kind: 'direct',
      title: 'Asha Rao',
      members: ['asha'],
      messages: [
        m('asha', 'Did you see the release notes?', 26 * HOUR),
        m('me', 'Yes, the new search is great', 25 * HOUR),
        m('asha', 'Can you review my PR before 5?', 12 * MIN),
        m('asha', "It's the one about the chat list", 11 * MIN),
      ],
      unread: 2,
      pinned: false,
      muted: false,
      createdAt: now - 90 * DAY,
    },
    {
      id: 'weekend',
      kind: 'group',
      title: 'Weekend trip ⛺',
      members: ['asha', 'ben', 'eli'],
      // Just created: nobody has written yet.
      messages: [],
      unread: 0,
      pinned: false,
      muted: false,
      createdAt: now - 8 * MIN,
    },
    {
      id: 'ben',
      kind: 'direct',
      title: 'Ben Okafor',
      members: ['ben'],
      messages: [m('ben', 'Leg day tomorrow?', 3 * HOUR), m('me', 'Always. 7am?', 2 * HOUR, 'delivered')],
      unread: 0,
      pinned: false,
      muted: false,
      createdAt: now - 60 * DAY,
    },
    {
      id: 'flat',
      kind: 'group',
      title: 'Flat 4B',
      members: ['fatima', 'hana', 'gabe'],
      messages: [
        m('fatima', 'Rent is due Friday', 2 * DAY),
        m('hana', 'Paid ✅', 2 * DAY - 3 * HOUR),
        m('gabe', 'Paying tonight, sorry!', 1 * DAY + 2 * HOUR),
        m('fatima', 'Also who left the oven on 😅', 5 * HOUR),
      ],
      unread: 4,
      pinned: false,
      muted: true,
      createdAt: now - 200 * DAY,
    },
    {
      id: 'chen',
      kind: 'direct',
      title: 'Chen Wei',
      members: ['chen'],
      messages: [m('me', 'Thanks for the book!', 4 * DAY), m('chen', 'Enjoy it 📚', 4 * DAY - 20 * MIN)],
      unread: 0,
      pinned: false,
      muted: false,
      createdAt: now - 120 * DAY,
    },
  ];

  const calls: Call[] = [
    { id: 'c1', contactId: 'asha', at: now - 40 * MIN, direction: 'missed', video: false },
    { id: 'c2', contactId: 'ben', at: now - 5 * HOUR, direction: 'outgoing', video: true },
    { id: 'c3', contactId: 'dana', at: now - 1 * DAY - 2 * HOUR, direction: 'incoming', video: false },
    { id: 'c4', contactId: 'hana', at: now - 3 * DAY, direction: 'incoming', video: true },
  ];

  return { contacts: Object.fromEntries(contacts.map((c) => [c.id, c])), chats, calls };
}
