package org.enough.app;
import org.junit.Test;
import static org.junit.Assert.*;
public class UsageTimelineTest {
 @Test public void activityFromPreviousDayCountsOnlyToday(){UsageTimeline t=new UsageTimeline(1000,2000);t.resume("app",500);assertEquals(Long.valueOf(1000),t.totals().get("app"));}
 @Test public void shutdownStopsForegroundTime(){UsageTimeline t=new UsageTimeline(0,10000);t.resume("app",1000);t.stopAll(2000);assertEquals(Long.valueOf(1000),t.totals().get("app"));assertEquals("",t.foreground());}
 @Test public void repeatedPauseDoesNotDoubleCount(){UsageTimeline t=new UsageTimeline(0,10000);t.resume("app",1000);t.pause("app",2000);t.pause("app",3000);assertEquals(Long.valueOf(1000),t.totals().get("app"));}
 @Test public void unrelatedPauseDoesNotStopSelectedApp(){UsageTimeline t=new UsageTimeline(0,3000);t.resume("app",1000);t.pause("other",2000);assertEquals(Long.valueOf(2000),t.totals().get("app"));}
 @Test public void outOfOrderEventsDoNotSubtractTime(){UsageTimeline t=new UsageTimeline(0,3000);t.resume("app",1000);t.pause("app",2000);t.resume("app",500);assertEquals(Long.valueOf(1000),t.totals().get("app"));}
 @Test public void totalsAreIdempotent(){UsageTimeline t=new UsageTimeline(0,3000);t.resume("app",1000);assertEquals(t.totals(),t.totals());}
}
