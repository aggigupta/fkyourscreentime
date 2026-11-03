export const MAX_MINUTES = 240;
export function validMinutes(value) {
  const n = Number(value);
  if (!Number.isInteger(n) || n < 1 || n > MAX_MINUTES) throw new Error('Choose 1–240 whole minutes.');
  return n;
}
function nextMidnight(now) { const d = new Date(now); d.setHours(24, 0, 0, 0); return d.getTime(); }
export function emptyDay(now) { return { version: 1, resetAt: nextMidnight(now), lastSeen: now, goalMinutes: null, tomorrowMinutes: null, usedMs: 0, session: null }; }
export function validate(raw) {
  if (!raw || raw.version !== 1 || !Number.isFinite(raw.resetAt) || !Number.isFinite(raw.lastSeen) || !Number.isFinite(raw.usedMs) || raw.usedMs < 0) throw new Error('Saved session data could not be read.');
  if (raw.goalMinutes !== null) validMinutes(raw.goalMinutes);
  if (raw.tomorrowMinutes !== null) validMinutes(raw.tomorrowMinutes);
  if (raw.usedMs > (raw.goalMinutes || 0) * 60_000) throw new Error('Saved session data is invalid.');
  if (raw.session && (![raw.session.endAt, raw.session.accountedUntil].every(Number.isFinite) || raw.session.endAt < raw.session.accountedUntil)) throw new Error('Saved session timing is invalid.');
  return structuredClone(raw);
}
export function advance(input, now) {
  let state = validate(input);
  const effectiveNow = Math.max(now, state.lastSeen);
  if (state.session) {
    const until = Math.min(effectiveNow, state.session.endAt, state.resetAt);
    const elapsed = Math.max(0, until - state.session.accountedUntil);
    state.usedMs = Math.min(state.goalMinutes * 60_000, state.usedMs + elapsed);
    state.session.accountedUntil = until;
    if (until >= state.session.endAt || state.usedMs >= state.goalMinutes * 60_000) state.session = null;
  }
  if (effectiveNow >= state.resetAt) {
    const goal = state.tomorrowMinutes ?? state.goalMinutes;
    state = emptyDay(effectiveNow);
    state.goalMinutes = goal;
  }
  state.lastSeen = effectiveNow;
  return state;
}
export function setGoal(input, minutes, now) {
  const state = advance(input, now);
  const goal = validMinutes(minutes);
  if (state.goalMinutes === null) state.goalMinutes = goal;
  else state.tomorrowMinutes = goal;
  return state;
}
export function remaining(state) { return Math.max(0, (state.goalMinutes || 0) * 60_000 - state.usedMs); }
export function startSession(input, minutes, now) {
  const state = advance(input, now);
  if (state.goalMinutes === null) throw new Error('Set your daily session budget first.');
  if (state.session) throw new Error('A session is already running in this browser.');
  if (remaining(state) <= 0) throw new Error('Your session budget is finished for today.');
  const length = Math.min(validMinutes(minutes) * 60_000, remaining(state));
  const begin = state.lastSeen;
  state.session = { accountedUntil: begin, endAt: Math.min(begin + length, state.resetAt) };
  return state;
}
export function endSession(input, now) { const state = advance(input, now); state.session = null; return state; }
