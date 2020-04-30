package org.spockframework.docs.utilities;

import java.time.*;
import java.util.function.Predicate;

// tag::age-filter-class[]
public class AgeFilter implements Predicate<LocalDate> {
  private final Clock clock;
  private final int age;

  public AgeFilter(Clock clock, int age) {                                       // <1>
    this.clock = clock;
    this.age = age;
  }

  @Override
  public boolean test(LocalDate date) {
    return Period.between(date, LocalDate.now(clock)).getYears() >= age;         // <2>
  }
}
// end::age-filter-class[]
