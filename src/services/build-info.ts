/**
 * What is actually running: app version, runtime version, update channel and
 * the EAS Update id (or "embedded" when the bundle shipped inside the binary).
 * Used by Settings and by Sentry (release, dist, tags).
 */
import Constants from 'expo-constants';
import * as Updates from 'expo-updates';
import { Platform } from 'react-native';

export const APP_ID = 'dev.softwarefactory.chatter';

export interface BuildInfo {
  appVersion: string;
  runtimeVersion: string;
  /** Channel the binary asks for updates on (e.g. "chat"). */
  channel: string;
  /** Running EAS Update id, or null for the embedded bundle. */
  updateId: string | null;
  /** updateId, or "embedded". */
  updateLabel: string;
  updatesEnabled: boolean;
  platform: string;
  /** Sentry release, e.g. dev.softwarefactory.chatter@1.0.0 */
  release: string;
  /** Sentry dist: the update id or "embedded". */
  dist: string;
}

function configuredChannel(): string | null {
  const headers = Constants.expoConfig?.updates?.requestHeaders as Record<string, string> | undefined;
  const fromHeaders = headers?.['expo-channel-name'];
  const fromExtra = Constants.expoConfig?.extra?.updateChannel;
  return fromHeaders ?? (typeof fromExtra === 'string' ? fromExtra : null);
}

/** expo-updates reports "" (not null) for channel/runtimeVersion when updates are disabled. */
function nonEmpty(value: string | null | undefined): string | null {
  return value ? value : null;
}

let cached: BuildInfo | null = null;

export function getBuildInfo(): BuildInfo {
  if (cached) return cached;
  const appVersion = Constants.expoConfig?.version ?? '0.0.0';
  const updateId = Updates.isEnabled && !Updates.isEmbeddedLaunch ? nonEmpty(Updates.updateId) : null;
  const updateLabel = updateId ?? 'embedded';
  cached = {
    appVersion,
    runtimeVersion: nonEmpty(Updates.runtimeVersion) ?? appVersion,
    channel: nonEmpty(Updates.channel) ?? configuredChannel() ?? 'none',
    updateId,
    updateLabel,
    updatesEnabled: Updates.isEnabled,
    platform: Platform.OS,
    release: `${APP_ID}@${appVersion}`,
    dist: updateLabel,
  };
  return cached;
}
