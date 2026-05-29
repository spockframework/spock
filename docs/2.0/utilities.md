# Utilities

## Testing Time with `MutableClock`

When working with dates or time we often have the problem of writing stable tests.
Java only provides a `FixedClock` for testing.
However, often time related code has to deal with the change of time,
so a fixed clock is not enough or makes the test harder to follow.


The prerequisite for using both `FixedClock` and Spocks `MutableClock` is that the production code,
actually uses a configurable `Clock` and not just the parameterless `Instant.now()`
or the corresponding methods in the other `java.time.*` classes.


### Example

**Class under Test**

```groovy
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
```


1. `Clock` is injected via constructor
2. `Clock` is used to get the current date


**Test**

```groovy
  def "AgeFilter reacts to time"() {
    given:
    ZonedDateTime defaultTime = ZonedDateTime.of(2018, 6, 5, 0, 0, 0, 0, ZoneId.of('UTC'))
    MutableClock clock = new MutableClock(defaultTime)                                       // <1>
    AgeFilter ageFilter = new AgeFilter(clock,18)                                            // <2>

    LocalDate birthday = defaultTime.minusYears(18).plusDays(1).toLocalDate()

    expect:
    !ageFilter.test(birthday)                                                                // <3>

    when:
    clock + Duration.ofDays(1)                                                               // <4>

    then:
    ageFilter.test(birthday)                                                                 // <5>
  }
}
```


1. `MutableClock` created with a well known time
2. `Clock` is injected via constructor
3. `age` is less than `18` so the result is `false`
4. the clock is advanced by one day
5. `age` is equal to `18` so the result is `true`


There are many more ways to modify `MutableClock` just have a look at the JavaDocs, or the test code `spock.util.time.MutableClockSpec`.

