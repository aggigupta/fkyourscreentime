package org.enough.app;
import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Process;
import java.util.HashMap;
import java.util.Map;

/** Reads activity lifecycle timestamps only; never persists the event stream. */
public final class UsageReader {
 private UsageReader(){}
 public static boolean allowed(Context context){AppOpsManager ops=(AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);return ops!=null&&ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,Process.myUid(),context.getPackageName())==AppOpsManager.MODE_ALLOWED;}
 public static final class Snapshot { public final Map<String,Long> totals=new HashMap<>(); public String foreground=""; }
 @SuppressWarnings("deprecation")
 public static Snapshot read(Context context,long start,long now){
  Snapshot result=new Snapshot(); if(!allowed(context)||now<start)return result;
  UsageStatsManager manager=(UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE); if(manager==null)return result;
  UsageEvents events=manager.queryEvents(Math.max(0,start-86_400_000L),now);if(events==null)return result;
  UsageEvents.Event event=new UsageEvents.Event();String active="";long since=start;
  while(events.hasNextEvent()){
   events.getNextEvent(event);int type=event.getEventType();long at=event.getTimeStamp();String pkg=event.getPackageName();
   if(type==UsageEvents.Event.ACTIVITY_RESUMED){
    accumulate(result,active,Math.max(start,since),Math.max(start,at));active=pkg==null?"":pkg;since=Math.max(start,at);
   }else if((type==UsageEvents.Event.ACTIVITY_PAUSED&&active.equals(pkg))||type==UsageEvents.Event.SCREEN_NON_INTERACTIVE){
    accumulate(result,active,Math.max(start,since),Math.max(start,at));active="";since=Math.max(start,at);
   }
  }
  accumulate(result,active,Math.max(start,since),now);result.foreground=active;return result;
 }
 private static void accumulate(Snapshot result,String pkg,long from,long to){if(!pkg.isEmpty()&&to>from)result.totals.merge(pkg,to-from,Long::sum);}
}
