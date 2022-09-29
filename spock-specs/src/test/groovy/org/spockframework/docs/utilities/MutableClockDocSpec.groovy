package org.spockframework.docs.utilities

import spock.lang.Specification
import spock.util.time.MutableClock

import java.time.*

class MutableClockDocSpec extends Specification {


// tag::age-filter-spec[]
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
// end::age-filter-spec[]
}
