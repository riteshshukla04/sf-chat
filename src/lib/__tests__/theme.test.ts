import { ForestPalette, Palette, resolveColors } from '../../constants/theme';

const rgb = (hex: string) => [1, 3, 5].map((i) => parseInt(hex.slice(i, i + 2), 16));

describe('Palette accent (button colour)', () => {
  it.each(['light', 'dark'] as const)('%s accent is blue', (scheme) => {
    const [r, g, b] = rgb(Palette[scheme].accent);
    expect(b).toBeGreaterThan(g);
    expect(b).toBeGreaterThan(r);
  });
});

describe('Forest theme', () => {
  it.each(['light', 'dark'] as const)('%s accent is green and has every colour key', (scheme) => {
    const [r, g, b] = rgb(ForestPalette[scheme].accent);
    expect(g).toBeGreaterThan(r);
    expect(g).toBeGreaterThan(b);
    expect(Object.keys(ForestPalette[scheme]).sort()).toEqual(Object.keys(Palette[scheme]).sort());
  });

  it('resolveColors picks palette by theme and scheme', () => {
    expect(resolveColors('dark', 'forest')).toBe(ForestPalette.dark);
    expect(resolveColors('light', 'forest')).toBe(ForestPalette.light);
    expect(resolveColors(null, 'default')).toBe(Palette.light);
    expect(resolveColors('dark', 'default')).toBe(Palette.dark);
  });
});
