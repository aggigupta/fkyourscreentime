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
 public static final class Snapshot { public final Map<String,Long> totals=new HashMap<>(); public String foreground=""; public boolean available,observed; }
 @SuppressWarnings("deprecation")
 public static Snapshot read(Context context,long start,long now){
  Snapshot result=new Snapshot(); if(!allowed(context)||now<start)return result;
  UsageStatsManager manager=(UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE); if(manager==null)return result;
  UsageEvents events=manager.queryEvents(Math.max(0,start-86_400_000L),now);if(events==null)return result;
  result.available=true;
  UsageEvents.Event event=new UsageEvents.Event();UsageTimeline timeline=new UsageTimeline(start,now);
  while(events.hasNextEvent()){
   events.getNextEvent(event);int type=event.getEventType();long at=event.getTimeStamp();String pkg=event.getPackageName();
   if(type==UsageEvents.Event.ACTIVITY_RESUMED){
    timeline.resume(pkg,at);
   }else if(type==UsageEvents.Event.ACTIVITY_PAUSED){
    timeline.pause(pkg,at);
   }else if(type==UsageEvents.Event.SCREEN_NON_INTERACTIVE||type==UsageEvents.Event.DEVICE_SHUTDOWN){
    timeline.stopAll(at);
   }
  }
  result.totals.putAll(timeline.totals());result.foreground=timeline.foreground();result.observed=timeline.observed();return result;
 }
}
