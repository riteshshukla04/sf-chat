/**
 * Crash reporting. Sentry is only initialised when EXPO_PUBLIC_SENTRY_DSN is
 * set at build/update time; otherwise every function here is a no-op.
 *
 * release = dev.softwarefactory.chatter@<app version>
 * dist    = running EAS Update id, or "embedded"
 * tags    = app, platform, update_id, channel, runtime_version
 */
import * as Sentry from '@sentry/react-native';
import type { ComponentType } from 'react';

import { getBuildInfo } from './build-info';

const DSN = process.env.EXPO_PUBLIC_SENTRY_DSN;

export const sentryEnabled = Boolean(DSN);

let initialised = false;

export function initSentry(): void {
  if (!DSN || initialised) return;
  initialised = true;
  const info = getBuildInfo();

  Sentry.init({
    dsn: DSN,
    release: info.release,
    dist: info.dist,
    environment: __DEV__ ? 'development' : info.channel,
    sendDefaultPii: false,
    tracesSampleRate: 0.2,
    debug: false,
  });

  Sentry.setTags({
    app: 'chatter',
    platform: info.platform,
    update_id: info.updateLabel,
    channel: info.channel,
    runtime_version: info.runtimeVersion,
  });
}

/** Wraps the root component (touch breadcrumbs, profiler) only when Sentry is on. */
export function wrapRoot<P extends Record<string, unknown>>(Root: ComponentType<P>): ComponentType<P> {
  return sentryEnabled ? Sentry.wrap(Root) : Root;
}

export function captureException(error: unknown): void {
  if (initialised) Sentry.captureException(error);
}

export function addBreadcrumb(message: string, data?: Record<string, unknown>): void {
  if (initialised) Sentry.addBreadcrumb({ category: 'chatter', message, data });
}
