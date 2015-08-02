package org.spockframework.smoke.mock

import spock.lang.Specification

class PartialMockingInterfacesWithDefaultMethods extends Specification {
  def "ISquare area should be computed using the stubbed length - test with when: and then: blocks"() {
    given:
    ISquare square = Spy() {
      2 * getLength() >> 3
    }

    when:
    def area = square.area

    then:
    area == 9
  }

  def "ISquare area should be computed using the stubbed length - test with when:, then: and where: blocks"() {
    given:
    ISquare square = Spy() {
      2 * getLength() >> len
    }

    when:
    def area = square.area

    then:
    area == ar

    where:
    len | ar
      3 |  9
      5 | 25
      7 | 49
  }

  def "ISquare area should be computed using the stubbed length - test with when: and then: blocks and various stub values"() {
    given:
    ISquare square = Spy() {
      2 * getLength() >> 3
      2 * getLength() >> 5
      2 * getLength() >> 7
    }

    when:
    def area1 = square.area
    def area2 = square.area
    def area3 = square.area

    then:
    area1 == 9
    area2 == 25
    area3 == 49
  }

  def "ISquare area should be computed using the stubbed length - test with expect: block"() {
    given:
    ISquare square = Spy() {
      2 * getLength() >> 3
    }

    expect:
    square.area == 9
  }

  def "ISquare area should be computed using the stubbed length - test with expect: and where: blocks"() {
    given:
    ISquare square = Spy() {
      2 * getLength() >> len
    }

    expect:
    square.area == ar

    where:
    len | ar
      3 |  9
      5 | 25
      7 | 49
  }

  def "ISquare area should be computed using the stubbed length - test with expect: block and various stub values"() {
    given:
    ISquare square = Spy() {
      2 * getLength() >> 3
      2 * getLength() >> 5
      2 * getLength() >> 7
    }

    expect:
    square.area == 9
    square.area == 25
    square.area == 49
  }
}
