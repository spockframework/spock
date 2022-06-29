/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.mock

import spock.lang.Specification

import static spock.util.matcher.HamcrestMatchers.closeTo

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


  def "IQuarterlyCompoundedDeposit: compound interest should be computed using the stubbed annual nominal interest rate"() {
    given:
    IQuarterlyCompoundedDeposit deposit = Spy() {
      1 * getAnnualNominalInterestRate() >> 0.043
    }

    when:
    def compoundInterest = deposit.getCompoundInterest(1500, 6)

    then:
    compoundInterest closeTo(438.84, 0.01)
  }

  def "IQuarterlyCompoundedDeposit: compound interest should be computed using the stubbed annual nominal interest rate and the stubbed compounding periods per year"() {
    given:
    IQuarterlyCompoundedDeposit deposit = Spy() {
      1 * getAnnualNominalInterestRate() >> 0.129  // stubbing an abstract method
      1 * getCompoundingPeriodsPerYear() >> 12     // stubbing a default method
    }

    expect:
    (deposit.getCompoundInterest(1500, 2)) closeTo(438.84, 0.01)
  }


  def "IQuarterlyCompoundedDeposit: should throw unchecked exception for negative values of principalAmount"() {
    given:
    IQuarterlyCompoundedDeposit deposit = Spy() {
      getAnnualNominalInterestRate() >> 0.043
    }

    when:
    deposit.getCompoundInterest(-1500, 6)

    then:
    thrown(IllegalArgumentException)
  }

  def "IQuarterlyCompoundedDeposit: should throw checked exception for negative values of numberOfYears"() {
    given:
    IQuarterlyCompoundedDeposit deposit = Spy() {
      getAnnualNominalInterestRate() >> 0.043
    }

    when:
    deposit.getCompoundInterest(1500, -6)

    then:
    thrown(IDeposit.DepositException)
  }

}
