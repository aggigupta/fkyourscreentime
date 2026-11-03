package org.enough.app;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Proposals can replace today's plan only before the first commitment. */
public final class PlanCommitment {
 public final Map<String,Long> today;
 public final Map<String,Long> tomorrow;
 public final boolean deferred;
 private PlanCommitment(Map<String,Long> today,Map<String,Long> tomorrow,boolean deferred){this.today=Collections.unmodifiableMap(new LinkedHashMap<>(today));this.tomorrow=Collections.unmodifiableMap(new LinkedHashMap<>(tomorrow));this.deferred=deferred;}
 public static PlanCommitment propose(Map<String,Long> active,Map<String,Long> proposed){return active.isEmpty()?new PlanCommitment(proposed,proposed,false):new PlanCommitment(active,proposed,true);}
}
