package org.enough.app;
import org.junit.Test;
import static org.junit.Assert.*;
import java.time.Instant;

public class BudgetRulesTest {
 @Test public void boundaryBlocksExactlyAtLimit(){assertFalse(BudgetRules.blocked(60_000,59_999));assertTrue(BudgetRules.blocked(60_000,60_000));assertTrue(BudgetRules.blocked(60_000,90_000));assertEquals(0,BudgetRules.remaining(60_000,90_000));}
 @Test public void unselectedAppsAreNeverBlocked(){assertFalse(BudgetRules.blocked(0,900_000));}
 @Test public void clockForwardDoesNotResetSameBoot(){assertFalse(BudgetRules.dayExpired(Long.MAX_VALUE,999,7,2000,1000,7));}
 @Test public void clockRollbackDoesNotDelaySameBootReset(){assertTrue(BudgetRules.dayExpired(1,1000,7,2000,1000,7));}
 @Test public void rebootUsesSavedWallDeadline(){assertFalse(BudgetRules.dayExpired(1999,5,8,2000,1000,7));assertTrue(BudgetRules.dayExpired(2000,5,8,2000,1000,7));}
 @Test public void midnightRespectsZone(){long now=Instant.parse("2026-11-03T12:00:00Z").toEpochMilli();assertEquals(Instant.parse("2026-11-03T18:30:00Z").toEpochMilli(),BudgetRules.nextMidnight(now,"Asia/Kolkata"));}
 @Test public void daylightSavingDayIsNotAssumed24Hours(){long start=Instant.parse("2026-03-08T05:00:00Z").toEpochMilli();assertEquals(23*3_600_000L,BudgetRules.nextMidnight(start,"America/New_York")-start);}
 @Test(expected=IllegalArgumentException.class) public void rejectZeroMinuteGoal(){BudgetRules.validateMinutes(0);}
 @Test(expected=IllegalArgumentException.class) public void rejectOversizedGoal(){BudgetRules.validateMinutes(241);}
}
