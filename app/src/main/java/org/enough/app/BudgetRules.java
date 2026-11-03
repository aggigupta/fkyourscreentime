package org.enough.app;

import java.time.Instant;
import java.time.ZoneId;

/** Platform-free policy: strict per-app totals and a same-boot monotonic day fence. */
public final class BudgetRules {
 private BudgetRules() {}
 public static long remaining(long limitMs, long usedMs) { return Math.max(0L, limitMs - Math.max(0L, usedMs)); }
 public static boolean blocked(long limitMs, long usedMs) { return limitMs > 0 && remaining(limitMs, usedMs) == 0; }
 public static long nextMidnight(long wallMs, String zone) {
  return Instant.ofEpochMilli(wallMs).atZone(ZoneId.of(zone)).toLocalDate().plusDays(1).atStartOfDay(ZoneId.of(zone)).toInstant().toEpochMilli();
 }
 public static boolean dayExpired(long nowWall, long nowElapsed, int boot, long resetWall, long resetElapsed, int savedBoot) {
  return boot == savedBoot ? nowElapsed >= resetElapsed : nowWall >= resetWall;
 }
 public static void validateMinutes(int minutes) { if(minutes < 1 || minutes > 240) throw new IllegalArgumentException("Choose 1–240 minutes."); }
}
