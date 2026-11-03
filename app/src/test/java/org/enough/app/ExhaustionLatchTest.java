package org.enough.app;
import org.junit.Test;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import static org.junit.Assert.*;
public class ExhaustionLatchTest {
 @Test public void exhaustedAppIsLatched(){assertTrue(ExhaustionLatch.merge(Set.of(),Map.of("instagram",60000L),Map.of("instagram",60000L)).contains("instagram"));}
 @Test public void emptyAndroidSnapshotCannotUnlock(){assertTrue(ExhaustionLatch.merge(Set.of("instagram"),Map.of("instagram",60000L),Map.of()).contains("instagram"));}
 @Test public void lowerCounterCannotUnlock(){assertTrue(ExhaustionLatch.merge(Set.of("instagram"),Map.of("instagram",60000L),Map.of("instagram",1L)).contains("instagram"));}
 @Test public void pendingPlanRemovalCannotUnlock(){assertTrue(ExhaustionLatch.merge(Set.of("instagram"),Map.of(),Map.of()).contains("instagram"));}
 @Test public void beforeLimitDoesNotLatch(){assertFalse(ExhaustionLatch.merge(Set.of(),Map.of("instagram",60000L),Map.of("instagram",59999L)).contains("instagram"));}
 @Test public void returnedSetDoesNotMutatePreviousDay(){Set<String> previous=new HashSet<>();ExhaustionLatch.merge(previous,Map.of("instagram",1L),Map.of("instagram",1L));assertTrue(previous.isEmpty());}
}
