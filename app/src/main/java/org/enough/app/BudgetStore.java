package org.enough.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.provider.Settings;
import org.json.JSONObject;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** One private local ledger. Today's plan has no editing or override API. */
public final class BudgetStore {
 private static BudgetStore instance;
 public static synchronized BudgetStore get(Context context) {
  if(instance == null) instance = new BudgetStore(context.getApplicationContext());
  return instance;
 }
 private final Context context;
 private final SharedPreferences prefs;
 private BudgetStore(Context context) { this.context=context; prefs=context.getSharedPreferences("budgets",Context.MODE_PRIVATE); ensureDay(); }
 private int boot() { return Settings.Global.getInt(context.getContentResolver(),Settings.Global.BOOT_COUNT,-1); }
 public synchronized void ensureDay() {
  long now=System.currentTimeMillis(), elapsed=SystemClock.elapsedRealtime(); int boot=boot();
  boolean initial=!prefs.contains("reset_wall");
  if(!initial && !BudgetRules.dayExpired(now,elapsed,boot,prefs.getLong("reset_wall",0),prefs.getLong("reset_elapsed",0),prefs.getInt("boot",-2))) return;
  String zone=ZoneId.systemDefault().getId(); long reset=BudgetRules.nextMidnight(now,zone);
  long start=Instant.ofEpochMilli(now).atZone(ZoneId.of(zone)).toLocalDate().atStartOfDay(ZoneId.of(zone)).toInstant().toEpochMilli();
  SharedPreferences.Editor edit=prefs.edit().putLong("reset_wall",reset).putLong("reset_elapsed",elapsed+reset-now).putInt("boot",boot).putLong("day_start",start).putString("zone",zone).putString("spent","{}");
  if(prefs.contains("next_plan")) edit.putString("plan",prefs.getString("next_plan","{}")).remove("next_plan");
  edit.commit();
 }
 private Map<String,Long> read(String key) {
  Map<String,Long> map=new LinkedHashMap<>();
  try { JSONObject obj=new JSONObject(prefs.getString(key,"{}")); Iterator<String> keys=obj.keys(); while(keys.hasNext()){String k=keys.next();map.put(k,obj.optLong(k,0));} } catch(Exception ignored) { /* Private data corruption must not crash the settings screen. */ }
  return map;
 }
 private String json(Map<String,Long> map){return new JSONObject(map).toString();}
 public synchronized Map<String,Long> plan(){ensureDay();return read("plan");}
 public synchronized Map<String,Long> nextPlan(){ensureDay();return prefs.contains("next_plan")?read("next_plan"):read("plan");}
 public synchronized boolean hasPlan(){return !plan().isEmpty();}
 public synchronized void savePlan(Map<String,Long> minutes) {
  ensureDay(); Map<String,Long> limits=new LinkedHashMap<>();
  for(Map.Entry<String,Long> entry:minutes.entrySet()) { BudgetRules.validateMinutes(Math.toIntExact(entry.getValue())); if(!PackageSafety.canLimit(context,entry.getKey()))throw new IllegalArgumentException("This app cannot be limited."); limits.put(entry.getKey(),entry.getValue()*60_000L); }
  // Once committed, changes (including removing apps) only apply after the day fence expires.
  PlanCommitment result=PlanCommitment.propose(read("plan"),limits);
  prefs.edit().putString(result.deferred?"next_plan":"plan",json(result.deferred?result.tomorrow:result.today)).commit();
 }
 public synchronized long limit(String pkg){return plan().getOrDefault(pkg,0L);}
 public synchronized long used(String pkg){ensureDay();return read("spent").getOrDefault(pkg,0L);}
 public synchronized void add(String pkg,long millis){
  ensureDay(); long limit=read("plan").getOrDefault(pkg,0L); if(limit<=0||millis<=0)return;
  Map<String,Long> spent=read("spent"); long old=spent.getOrDefault(pkg,0L);
  spent.put(pkg,Math.min(limit,old+Math.min(millis,limit)));prefs.edit().putString("spent",json(spent)).commit();
 }
 public synchronized void reconcile(Map<String,Long> totals){
  ensureDay(); Map<String,Long> spent=read("spent"),plan=read("plan");
  for(String pkg:plan.keySet())spent.put(pkg,Math.min(plan.get(pkg),Math.max(spent.getOrDefault(pkg,0L),totals.getOrDefault(pkg,0L))));
  prefs.edit().putString("spent",json(spent)).commit();
 }
 public synchronized boolean blocked(String pkg){return BudgetRules.blocked(limit(pkg),used(pkg));}
 public synchronized long dayStart(){ensureDay();return prefs.getLong("day_start",0);}
 public synchronized long resetAt(){ensureDay();return prefs.getLong("reset_wall",0);}
 public synchronized String zone(){ensureDay();return prefs.getString("zone",ZoneId.systemDefault().getId());}
 public void setDisclosureAccepted(){prefs.edit().putBoolean("disclosure",true).commit();}
 public boolean disclosureAccepted(){return prefs.getBoolean("disclosure",false);}
 public void heartbeat(){prefs.edit().putLong("heartbeat",SystemClock.elapsedRealtime()).apply();}
 public boolean live(){long last=prefs.getLong("heartbeat",-100_000);return last>0&&SystemClock.elapsedRealtime()-last>=0&&SystemClock.elapsedRealtime()-last<5000;}
}
