import {emptyDay, validate, advance, setGoal, startSession, endSession, remaining} from './focus-engine.js';
const KEY = 'enough.browser-budget.v1';
const $ = id => document.getElementById(id);
const goalForm = $('goal-form'), sessionForm = $('session-form');
let storageFailed = false;
function read() { const raw = localStorage.getItem(KEY); return raw ? validate(JSON.parse(raw)) : emptyDay(Date.now()); }
function save(state) { localStorage.setItem(KEY, JSON.stringify(state)); }
function format(ms) { const seconds=Math.ceil(Math.max(0,ms)/1000);return `${String(Math.floor(seconds/60)).padStart(2,'0')}:${String(seconds%60).padStart(2,'0')}`; }
function minutes(ms) { return `${Math.ceil(Math.max(0,ms)/60000)}m`; }
function fail(error) {
 storageFailed=true;
 $('focus-error').textContent=`${error.message || 'Browser storage is unavailable.'} Browser mode needs working local storage. The Android download is still available.`;
 $('save-status').textContent='Not saved';
 for(const control of document.querySelectorAll('.focus-panel button,.focus-panel input'))control.disabled=true;
}
function render(state, syncInputs=false) {
 const left=remaining(state), running=!!state.session;
 $('timer').textContent=running?format(state.session.endAt-state.lastSeen):format(left);
 $('timer').setAttribute('aria-label',`${running?'Session time remaining':'Daily session allowance remaining'}: ${$('timer').textContent}`);
 $('session-status').textContent=running?'Session running. Time keeps counting when you leave this page.':state.goalMinutes===null?'Set your daily session budget to begin.':left<=0?'Your browser-session budget is done for today.':'Ready for an intentional browsing session.';
 $('usage-progress').max=(state.goalMinutes||1)*60000;
 $('usage-progress').value=state.usedMs;
 $('used-label').textContent=`${minutes(state.usedMs)} used`;
 $('remaining-label').textContent=state.goalMinutes===null?'No budget set':`${minutes(left)} left today`;
 $('save-goal').textContent=state.goalMinutes===null?'Commit daily budget':'Plan tomorrow’s budget';
 $('start-session').disabled=storageFailed||running||state.goalMinutes===null||left<=0;
 $('session-minutes').disabled=storageFailed||running||left<=0;
 $('end-session').hidden=!running;
 $('tomorrow-note').textContent=state.tomorrowMinutes===null?(state.goalMinutes===null?'':'Today’s budget is committed. Further edits apply tomorrow.'):`Tomorrow’s budget: ${state.tomorrowMinutes} minutes. Today stays at ${state.goalMinutes} ${state.goalMinutes===1?"minute":"minutes"}.`;
 $('reset-label').textContent=`Next daily reset: ${new Date(state.resetAt).toLocaleString(undefined,{weekday:'short',hour:'2-digit',minute:'2-digit',timeZoneName:'short'})}.`;
 if(syncInputs)$('daily-minutes').value=state.tomorrowMinutes??state.goalMinutes??20;
 document.title=running?`${format(state.session.endAt-state.lastSeen)} · Enough`:'Enough — Make room for your day';
}
function action(change) {
 if(storageFailed)return;
 try {
  const next=change(read());save(next);$('focus-error').textContent='';render(next,true);
 } catch(error) {
  if(error instanceof DOMException || error instanceof SyntaxError)fail(error);
  else $('focus-error').textContent=error.message;
 }
}
goalForm.addEventListener('submit',event=>{event.preventDefault();action(state=>setGoal(state,$('daily-minutes').value,Date.now()));});
sessionForm.addEventListener('submit',event=>{event.preventDefault();action(state=>startSession(state,$('session-minutes').value,Date.now()));});
$('end-session').addEventListener('click',()=>action(state=>endSession(state,Date.now())));
function refresh(syncInputs=false) {
 if(storageFailed)return;
 try {
  const current=read();const now=Date.now();
  const next=advance(current,now);
  if(current.session||now>=current.resetAt)save(next);
  render(next,syncInputs);
 } catch(error){fail(error);}
}
window.addEventListener('storage',event=>{if(event.key===KEY)refresh(true);});
document.addEventListener('visibilitychange',()=>refresh());
refresh(true);
setInterval(()=>refresh(),1000);
if('serviceWorker' in navigator)navigator.serviceWorker.register('/sw.js').catch(()=>{/* Offline caching is optional; the timer still works online. */});
