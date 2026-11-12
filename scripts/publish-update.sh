#!/usr/bin/env bash
# Publish an EAS Update to the "chat" branch as a staged rollout.
#
#   ./scripts/publish-update.sh <rollout-percent> "<message>"
#   ./scripts/publish-update.sh 20 "Chatter 1.0.1: reactions preview"
#
# Optional env:
#   PLATFORM=android|ios|all   (default: all)
#   EXPO_PUBLIC_SENTRY_DSN, EXPO_PUBLIC_RELEASE_NOTE   inlined into the bundle (the note shows under "Chatter")
#
# Needs: `eas login` (or EXPO_TOKEN). The EAS project id comes from app.config.ts.
# Later, move the rollout up/down or back out:
#   npx eas-cli update:edit <group-id> --rollout-percentage 50 --branch chat
#   npx eas-cli update:revert-update-rollout
set -euo pipefail

usage() {
  echo "usage: $0 <rollout-percent 1-100> \"<message>\"" >&2
  exit 64
}

[[ $# -eq 2 ]] || usage
PERCENT="$1"
MESSAGE="$2"
BRANCH="chat"
PLATFORM="${PLATFORM:-all}"

if ! [[ "$PERCENT" =~ ^[0-9]+$ ]] || (( PERCENT < 1 || PERCENT > 100 )); then
  echo "error: rollout percent must be an integer from 1 to 100, got '$PERCENT'" >&2
  exit 64
fi
if [[ -z "${MESSAGE// /}" ]]; then
  echo "error: message must not be empty" >&2
  exit 64
fi

cd "$(dirname "$0")/.."

echo "Publishing to branch '$BRANCH' at ${PERCENT}% (platform: $PLATFORM, note: ${EXPO_PUBLIC_RELEASE_NOTE:-none})"

# 100 means everyone: a plain update. EAS takes a rollout percentage only below 100.
ROLLOUT=()
(( PERCENT < 100 )) && ROLLOUT=(--rollout-percentage "$PERCENT")

# --environment is required by eas-cli for SDK 55+ projects.
exec npx eas-cli@latest update \
  --branch "$BRANCH" \
  --environment production \
  --platform "$PLATFORM" \
  ${ROLLOUT[@]+"${ROLLOUT[@]}"} \
  --message "$MESSAGE" \
  --non-interactive
