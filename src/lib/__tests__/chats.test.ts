import {
  advanceStatus,
  canSend,
  type Chat,
  type Contact,
  dayLabel,
  draftMessage,
  formatTime,
  groupByDay,
  headerSubtitle,
  initials,
  previewText,
  searchChats,
  sortChats,
  totalUnread,
  withMessage,
} from '../chats';

// Thursday 12 November 2026, 15:30 local time.
const NOW = new Date(2026, 10, 12, 15, 30).getTime();
const at = (day: number, hour: number, minute = 0) => new Date(2026, 10, day, hour, minute).getTime();

const contacts: Record<string, Contact> = {
  asha: { id: 'asha', name: 'Asha Rao', about: '', lastSeenAt: null },
  ben: { id: 'ben', name: 'Ben Okafor', about: '', lastSeenAt: at(12, 9, 5) },
  chen: { id: 'chen', name: 'Chen Wei', about: '', lastSeenAt: at(9, 20) },
};

function chat(id: string, overrides: Partial<Chat> = {}): Chat {
  return { id, kind: 'direct', title: id, members: [id], messages: [], unread: 0, pinned: false, muted: false, createdAt: at(1, 9), ...overrides };
}
const msg = (id: string, from: string, text: string, sentAt: number) => ({ id, from, text, sentAt, status: 'read' as const });

describe('formatTime', () => {
  test('today shows the clock, then yesterday, the weekday, and a date', () => {
    expect(formatTime(at(12, 9, 7), NOW)).toBe('09:07');
    expect(formatTime(at(11, 23, 59), NOW)).toBe('Yesterday');
    expect(formatTime(at(9, 12), NOW)).toBe('Monday');
    expect(formatTime(at(2, 12), NOW)).toBe('02/11/2026');
  });
  test('day headers say Today instead of a clock', () => {
    expect(dayLabel(at(12, 8), NOW)).toBe('Today');
    expect(dayLabel(at(11, 8), NOW)).toBe('Yesterday');
  });
});

describe('the chat list', () => {
  test('pinned first, then the most recent activity', () => {
    const old = chat('old', { messages: [msg('1', 'ben', 'hi', at(10, 9))] });
    const recent = chat('recent', { messages: [msg('2', 'asha', 'hey', at(12, 14))] });
    const pinned = chat('pinned', { pinned: true, messages: [msg('3', 'chen', 'yo', at(3, 9))] });
    expect(sortChats([old, recent, pinned]).map((c) => c.id)).toEqual(['pinned', 'recent', 'old']);
  });
  test('previews say who wrote the last message', () => {
    expect(previewText(chat('a', { messages: [msg('1', 'me', 'On my way', NOW)] }), contacts)).toBe('You: On my way');
    expect(previewText(chat('g', { kind: 'group', members: ['asha', 'ben'], messages: [msg('1', 'ben', 'Pizza?', NOW)] }), contacts)).toBe('Ben: Pizza?');
    expect(previewText(chat('b', { messages: [msg('1', 'ben', 'Sure', NOW)] }), contacts)).toBe('Sure');
    expect(previewText(chat('new'), contacts)).toBe('No messages yet');
  });
  test('the unread badge leaves muted chats out', () => {
    expect(totalUnread([chat('a', { unread: 2 }), chat('b', { unread: 3, muted: true }), chat('c', { unread: 1 })])).toBe(3);
  });
  test('search matches titles and message text', () => {
    const chats = [chat('Asha Rao', { title: 'Asha Rao' }), chat('Ben', { title: 'Ben', messages: [msg('1', 'ben', 'Movie tonight?', NOW)] })];
    expect(searchChats(chats, 'asha').map((c) => c.id)).toEqual(['Asha Rao']);
    expect(searchChats(chats, 'MOVIE').map((c) => c.id)).toEqual(['Ben']);
    expect(searchChats(chats, '  ')).toHaveLength(2);
  });
});

describe('a conversation', () => {
  test('a direct chat says when the contact was last seen', () => {
    expect(headerSubtitle(chat('asha'), contacts, NOW)).toBe('online');
    expect(headerSubtitle(chat('ben'), contacts, NOW)).toBe('last seen today at 09:05');
    expect(headerSubtitle(chat('chen'), contacts, NOW)).toBe('last seen monday');
  });
  test('a group lists its members and its last message', () => {
    const group = chat('g', { kind: 'group', title: 'Flat 4B', members: ['asha', 'ben'], messages: [msg('1', 'asha', 'Rent paid', at(12, 10, 15))] });
    expect(headerSubtitle(group, contacts, NOW)).toBe('Asha, Ben, You · last message 10:15');
  });
  test('sending: only real text, and the chat counts as read', () => {
    expect(canSend('   ')).toBe(false);
    expect(canSend(' hi ')).toBe(true);
    expect(canSend('x'.repeat(4001))).toBe(false);
    const sent = withMessage(chat('a', { unread: 4 }), draftMessage('  Hello  ', NOW, 'm1'));
    expect(sent.messages).toEqual([{ id: 'm1', from: 'me', text: 'Hello', sentAt: NOW, status: 'sending' }]);
    expect(sent.unread).toBe(0);
  });
  test('ticks move on one step at a time', () => {
    expect(advanceStatus('sending')).toBe('sent');
    expect(advanceStatus('sent')).toBe('delivered');
    expect(advanceStatus('delivered')).toBe('read');
    expect(advanceStatus('read')).toBe('read');
  });
  test('messages are grouped under day headers', () => {
    const groups = groupByDay([msg('1', 'me', 'a', at(11, 9)), msg('2', 'ben', 'b', at(11, 10)), msg('3', 'me', 'c', at(12, 9))], NOW);
    expect(groups.map((g) => [g.day, g.messages.length])).toEqual([
      ['Yesterday', 2],
      ['Today', 1],
    ]);
  });
  test('avatars show initials', () => {
    expect(initials('Asha Rao')).toBe('AR');
    expect(initials('Weekend trip ⛺')).toBe('WT');
    expect(initials('')).toBe('?');
  });
});
