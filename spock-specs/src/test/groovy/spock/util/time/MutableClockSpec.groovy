package spock.util.time

import spock.lang.*

import java.time.*
import java.time.temporal.*

import static java.time.ZoneOffset.UTC

class MutableClockSpec extends Specification {
  @Shared
  ZonedDateTime defaultTime = ZonedDateTime.of(2000, 6, 5, 12, 30, 30, 100, UTC)

  @Shared
  Instant defaultInstant = defaultTime.toInstant()

  @Subject
  MutableClock mutableClock = new MutableClock(defaultTime)

  def "MutableClock(ZonedDateTime) uses instant and zone from ZonedDateTime"() {
    expect:
    mutableClock.instant() == defaultInstant
    mutableClock.zone == defaultTime.zone
  }

  def "MutableClock() initialized with current time and default zone"() {
    given:
    Instant start = Instant.now()
    mutableClock = new MutableClock()
    Instant end = Instant.now()

    expect:
    with(mutableClock.instant()) {
      !it.isBefore(start)
      !it.isAfter(end)
    }
    mutableClock.zone == ZoneId.systemDefault()
  }

  def "MutableClock(ZoneId) initialized with current time and given zone"() {
    given:
    def zoneId = ZoneId.of("CET")
    Instant start = Instant.now()
    mutableClock = new MutableClock(zoneId)
    Instant end = Instant.now()

    expect:
    with(mutableClock.instant()) {
      !it.isBefore(start)
      !it.isAfter(end)
    }
    mutableClock.zone == zoneId
  }

  def "MutableClock(Instant) initialized with default zone"() {
    given:
    mutableClock = new MutableClock(defaultInstant)

    expect:
    mutableClock.instant() == defaultInstant
    mutableClock.zone == ZoneId.systemDefault()
  }

  def "MutableClock.withZone(ZoneId) returns a new instance with different zone"() {
    given:
    MutableClock other = mutableClock.withZone(ZoneId.of("CET"))

    expect:
    !mutableClock.is(other)
    other.zone == ZoneId.of("CET")
    mutableClock.instant() == other.instant()
  }

  @Unroll("MutableClock(#instant, #zone, #changeAmount) throws AssertionError")
  def "MutableClock may not be initialized with null values"(Instant instant, ZoneId zone, TemporalAmount changeAmount) {
    when:
    new MutableClock(instant, zone, changeAmount)

    then:
    thrown(NullPointerException)

    where:
    [instant, zone, changeAmount] << [
      [defaultInstant, null],
      [UTC, null],
      [Duration.ofSeconds(1), null]
    ].combinations().tail()
  }

  def "MutableClock to String includes fields"() {
    expect:
    mutableClock.toString() == 'MutableClock{zone=Z, instant=2000-06-05T12:30:30.000000100Z, changeAmount=PT1S}'
  }

  def "MutableClock always returns same instant if not modified"() {
    expect:
    mutableClock.instant() == defaultInstant

    when:
    sleep(100)

    then:
    mutableClock.instant() == defaultInstant
  }

  def "MutableClock can be set directly"() {
    given:
    Instant other = Instant.now()

    expect:
    mutableClock.instant() == defaultInstant

    when:
    mutableClock.instant = other

    then:
    mutableClock.instant() == other
  }

  def "MutableClock instant can't be null"() {
    when:
    mutableClock.instant = null

    then:
    thrown(NullPointerException)
  }

  def "MutableClock can be advanced with plus"(int amount) {
    expect:
    mutableClock.instant() == defaultInstant

    when:
    mutableClock + Duration.ofSeconds(amount)

    then:
    mutableClock.instant() == defaultInstant.plusSeconds(amount)

    where:
    amount << (1..10)
  }

  def "MutableClock can be rewound with minus"(int amount) {
    expect:
    mutableClock.instant() == defaultInstant

    when:
    mutableClock - Duration.ofSeconds(1)

    then:
    mutableClock.instant() == defaultInstant.minusSeconds(1)

    where:
    amount << (1..10)
  }

  def "MutableClock and use next to advance by specified amount"(TemporalAmount amount) {
    given:
    mutableClock.changeAmount = amount

    expect:
    mutableClock.instant() == defaultInstant

    when:
    mutableClock++

    then:
    mutableClock.instant() == defaultInstant + amount

    where:
    amount << [Duration.ofSeconds(1), Duration.ofHours(1), Duration.ofDays(1), Period.ofDays(1)]
  }

  def "MutableClock and use next to rewind by specified amount"(TemporalAmount amount) {
    given:
    mutableClock.changeAmount = amount

    expect:
    mutableClock.instant() == defaultInstant

    when:
    mutableClock--

    then:
    mutableClock.instant() == defaultInstant - amount

    where:
    amount << [Duration.ofSeconds(1), Duration.ofHours(1), Duration.ofDays(1), Period.ofDays(1)]
  }

  def "changeAmount can't be null"() {
    when:
    mutableClock.changeAmount = null

    then:
    thrown(NullPointerException)
  }

  def "MutableClock can be adjusted with TemporalAdjuster #adjusterName"(TemporalAdjuster adjuster, ZonedDateTime expected) {
    expect:
    mutableClock.instant() == defaultInstant

    when:
    mutableClock.adjust(adjuster)

    then:
    mutableClock.instant() == expected.toInstant()

    where:
    adjuster                            | adjusterName      | expected
    TemporalAdjusters.firstDayOfMonth() | "firstDayOfMonth" | ZonedDateTime.of(2000, 6, 1, 12, 30, 30, 100, UTC)
    TemporalAdjusters.firstDayOfYear()  | "firstDayOfYear"  | ZonedDateTime.of(2000, 1, 1, 12, 30, 30, 100, UTC)
    TemporalAdjusters.lastDayOfMonth()  | "lastDayOfMonth"  | ZonedDateTime.of(2000, 6, 30, 12, 30, 30, 100, UTC)
    TemporalAdjusters.lastDayOfYear()   | "lastDayOfYear"   | ZonedDateTime.of(2000, 12, 31, 12, 30, 30, 100, UTC)
  }

  def "MutableClock can be modified with a function #description"(Closure modification, Instant expected) {
    expect:
    mutableClock.instant() == defaultInstant

    when:
    mutableClock.modify(modification)

    then:
    mutableClock.instant() == expected

    where:
    description      | modification                                                                                                  | expected
    'plusSeconds'    | { Instant i, ZoneId z -> i.plusSeconds(1) }                                                                   | defaultInstant.plusSeconds(1)
    'firstDayOfYear' | { Instant i, ZoneId z -> ZonedDateTime.ofInstant(i, z).with(TemporalAdjusters.firstDayOfYear()).toInstant() } | ZonedDateTime.of(2000, 1, 1, 12, 30, 30, 100, UTC).toInstant()
  }
}
