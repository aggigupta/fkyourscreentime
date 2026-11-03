package org.enough.app;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** A day's exhausted apps can only grow. BudgetStore clears this solely at the day fence. */
public final class ExhaustionLatch {
 private ExhaustionLatch() {}
 public static Set<String> merge(Set<String> previous, Map<String,Long> limits, Map<String,Long> spent) {
  Set<String> result=new HashSet<>(previous);
  for(Map.Entry<String,Long> entry:limits.entrySet()) {
   if(BudgetRules.blocked(entry.getValue(),spent.getOrDefault(entry.getKey(),0L)))result.add(entry.getKey());
  }
  return result;
 }
}
