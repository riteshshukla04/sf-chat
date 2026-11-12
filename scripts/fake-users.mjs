#!/usr/bin/env node
/**
 * A stand-in for Chatter's backend (it has none) during rollout tests. It pretends users of the new over-the-air
 * update and of the current one, and every few seconds sends their crash rate to Software Factory the way
 * any app backend would: POST /ingest/signals with a bearer token. It knows nothing else about Software
 * Factory, and its numbers are made up on purpose.
 *
 *   SF_INGEST_TOKEN=... node scripts/fake-users.mjs --project <Software Factory project id> --group <EAS update group id> \
 *     [--scenario healthy|crashy] [--every 15] [--minutes 5] [--url http://127.0.0.1:4000]
 *
 * Signals are labelled { rollout_group: <group>, arm: "new" | "control" }, the labels an EAS Update rollout uses.
 */
const args = Object.fromEntries(
  process.argv.slice(2).reduce((pairs, arg, i, all) => (arg.startsWith("--") ? [...pairs, [arg.slice(2), all[i + 1]]] : pairs), []),
);
const url = (args.url ?? process.env.SF_URL ?? "http://127.0.0.1:4000").replace(/\/+$/, "");
const token = process.env.SF_INGEST_TOKEN;
const project = args.project;
const group = args.group;
const scenario = args.scenario ?? "healthy";
const every = Number(args.every ?? 15);
const minutes = Number(args.minutes ?? 5);

if (!token || !project || !group) {
  console.error("usage: SF_INGEST_TOKEN=... node scripts/fake-users.mjs --project <id> --group <EAS update group id> [--scenario healthy|crashy]");
  process.exit(64);
}

/** Crash rate per session of each version. The current version is steady; the new one depends on the scenario. */
const RATES = {
  healthy: { new: 0.0038, control: 0.004 },
  crashy: { new: 0.03, control: 0.004 },
};
if (!RATES[scenario]) {
  console.error(`unknown scenario "${scenario}"`);
  process.exit(64);
}
/** Sessions per batch: the new update reaches fewer people than the current one. */
const SESSIONS = { new: 4000, control: 16000 };

/** About n·p crashes, with the spread a real count would have. */
function crashes(n, p) {
  const u = Math.max(Math.random(), 1e-12);
  const gaussian = Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * Math.PI * Math.random());
  return Math.max(0, Math.round(n * p + gaussian * Math.sqrt(n * p * (1 - p))));
}

async function send() {
  const at = new Date().toISOString();
  const points = ["new", "control"].map((arm) => ({
    name: "crash_rate",
    value: crashes(SESSIONS[arm], RATES[scenario][arm]) / SESSIONS[arm],
    samples: SESSIONS[arm],
    labels: { rollout_group: group, arm },
    at,
  }));
  const res = await fetch(`${url}/ingest/signals`, {
    method: "POST",
    headers: { "content-type": "application/json", authorization: `Bearer ${token}` },
    body: JSON.stringify({ projectId: project, points }),
  });
  const body = await res.text();
  if (res.status !== 202) throw new Error(`Software Factory refused the signals: ${res.status} ${body}`);
  console.log(`${at.slice(11, 19)} ${scenario}: new ${(points[0].value * 100).toFixed(2)}%, control ${(points[1].value * 100).toFixed(2)}% → ${body}`);
}

const until = Date.now() + minutes * 60_000;
while (Date.now() < until) {
  await send();
  await new Promise((resolve) => setTimeout(resolve, every * 1000));
}
