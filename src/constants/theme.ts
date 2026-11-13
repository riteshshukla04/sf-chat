import { useColorScheme } from 'react-native';

import { useAppState } from '@/state/store';

export const Palette = {
  light: {
    background: '#FFFFFF',
    surface: '#FFFFFF',
    surfaceAlt: '#F2F4F3',
    chatBackground: '#EFEAE2',
    text: '#111B21',
    muted: '#667781',
    border: '#E4E8E6',
    accent: '#1A73E8',
    accentStrong: '#1557B0',
    onAccent: '#FFFFFF',
    bubbleMine: '#D6E8FF',
    bubbleTheirs: '#FFFFFF',
    tickRead: '#2E9BF0',
    danger: '#C9302C',
  },
  dark: {
    background: '#0B141A',
    surface: '#111B21',
    surfaceAlt: '#1F2C34',
    chatBackground: '#0B141A',
    text: '#E9EDEF',
    muted: '#8696A0',
    border: '#22303A',
    accent: '#4C9AFF',
    accentStrong: '#1A73E8',
    onAccent: '#0B141A',
    bubbleMine: '#1B4F8F',
    bubbleTheirs: '#1F2C34',
    tickRead: '#53BDEB',
    danger: '#FF6B66',
  },
};

export type Colors = (typeof Palette)['light'];

/** Forest: a green accent with moss/pine surfaces, each with a light and a dark variant. */
export const ForestPalette: Record<'light' | 'dark', Colors> = {
  light: {
    background: '#F7FAF6',
    surface: '#F7FAF6',
    surfaceAlt: '#E6EFE5',
    chatBackground: '#DCE8D8',
    text: '#14231A',
    muted: '#5E7466',
    border: '#D3E0D2',
    accent: '#2E7D4F',
    accentStrong: '#1F5C38',
    onAccent: '#FFFFFF',
    bubbleMine: '#C8E6C4',
    bubbleTheirs: '#FFFFFF',
    tickRead: '#2E9B5A',
    danger: '#C9302C',
  },
  dark: {
    background: '#0C1A12',
    surface: '#12251A',
    surfaceAlt: '#1B3325',
    chatBackground: '#0C1A12',
    text: '#E3EFE6',
    muted: '#8AA595',
    border: '#234032',
    accent: '#5FBF84',
    accentStrong: '#2E7D4F',
    onAccent: '#0C1A12',
    bubbleMine: '#1F5C38',
    bubbleTheirs: '#1B3325',
    tickRead: '#6FD99A',
    danger: '#FF6B66',
  },
};

export type ThemeName = 'default' | 'forest';
export const THEME_NAMES: ThemeName[] = ['default', 'forest'];
export const THEME_LABELS: Record<ThemeName, string> = { default: 'Default (blue)', forest: 'Forest (green)' };

export function resolveColors(scheme: 'light' | 'dark' | null | undefined, theme: ThemeName): Colors {
  const mode = scheme === 'dark' ? 'dark' : 'light';
  return (theme === 'forest' ? ForestPalette : Palette)[mode];
}

export function useColors(): Colors {
  const theme = useAppState((s) => s.theme);
  return resolveColors(useColorScheme(), theme);
}

export const Spacing = { xs: 4, sm: 8, md: 12, lg: 16, xl: 24 };
