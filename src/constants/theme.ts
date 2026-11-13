import { useColorScheme } from 'react-native';

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
    bubbleMine: '#D7F5DE',
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
    bubbleMine: '#0F5E44',
    bubbleTheirs: '#1F2C34',
    tickRead: '#53BDEB',
    danger: '#FF6B66',
  },
};

export type Colors = (typeof Palette)['light'];

export function useColors(): Colors {
  return useColorScheme() === 'dark' ? Palette.dark : Palette.light;
}

export const Spacing = { xs: 4, sm: 8, md: 12, lg: 16, xl: 24 };
