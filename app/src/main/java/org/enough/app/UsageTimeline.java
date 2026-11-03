package org.enough.app;

import java.util.HashMap;
import java.util.Map;

/** Deterministic foreground accounting; a shutdown must not count as continuing app use. */
public final class UsageTimeline {
 private final long start,end;
 private long since,lastEvent=Long.MIN_VALUE;
 private String active="";
 private boolean observed;
 private final Map<String,Long> totals=new HashMap<>();
 public UsageTimeline(long start,long end){this.start=start;this.end=end;since=start;}
 private boolean valid(long at){if(at>end||at<lastEvent)return false;lastEvent=at;observed=true;return true;}
 private void accumulate(long at){long from=Math.max(start,since),to=Math.max(start,at);if(!active.isEmpty()&&to>from)totals.merge(active,to-from,Long::sum);since=to;}
 public void resume(String pkg,long at){if(!valid(at))return;accumulate(at);active=pkg==null?"":pkg;}
 public void pause(String pkg,long at){if(!valid(at))return;if(active.equals(pkg)){accumulate(at);active="";}}
 public void stopAll(long at){if(!valid(at))return;accumulate(at);active="";}
 public Map<String,Long> totals(){Map<String,Long> result=new HashMap<>(totals);long from=Math.max(start,since);if(!active.isEmpty()&&end>from)result.merge(active,end-from,Long::sum);return result;}
 public String foreground(){return active;}
 public boolean observed(){return observed;}
}
