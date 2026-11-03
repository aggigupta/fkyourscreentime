package org.enough.app;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.LinkedHashMap;
import java.util.Map;
public class PlanCommitmentTest {
 @Test public void firstPlanAppliesToday(){PlanCommitment p=PlanCommitment.propose(Map.of(),Map.of("social",60000L));assertFalse(p.deferred);assertEquals(Long.valueOf(60000),p.today.get("social"));}
 @Test public void raisingBudgetCannotUnlockToday(){PlanCommitment p=PlanCommitment.propose(Map.of("social",60000L),Map.of("social",900000L));assertTrue(p.deferred);assertEquals(Long.valueOf(60000),p.today.get("social"));assertEquals(Long.valueOf(900000),p.tomorrow.get("social"));}
 @Test public void removingAppCannotUnlockToday(){PlanCommitment p=PlanCommitment.propose(Map.of("social",60000L),Map.of());assertTrue(p.today.containsKey("social"));assertTrue(p.tomorrow.isEmpty());}
 @Test public void newAppsDoNotMutateCommittedPlan(){PlanCommitment p=PlanCommitment.propose(Map.of("social",60000L),Map.of("game",120000L));assertFalse(p.today.containsKey("game"));assertTrue(p.today.containsKey("social"));assertTrue(p.tomorrow.containsKey("game"));}
 @Test public void callerCannotMutatePlanAfterCommit(){Map<String,Long> input=new LinkedHashMap<>();input.put("social",60000L);PlanCommitment p=PlanCommitment.propose(Map.of(),input);input.clear();assertTrue(p.today.containsKey("social"));}
}
