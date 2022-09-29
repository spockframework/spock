/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.util.matcher

import org.spockframework.runtime.ConditionNotSatisfiedError

import spock.lang.*

import static HamcrestMatchers.closeTo
import static HamcrestSupport.that
import static org.hamcrest.CoreMatchers.not

class IsCloseToSpec extends Specification {
  def "compare Integers that are close enough"() {
    expect:
    3 closeTo(2, 1)
    3 closeTo(3, 0)
    3 closeTo(4, 1)
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "compare Integers that are not close enough"() {
    expect:
    3 closeTo(1, 1)
  }

  def "compare BigDecimals that are close enough"() {
    expect:
    3.1415 closeTo(3.1, 0.05)
    3.1415 closeTo(3.1415000, 0)
    3.1415 closeTo(3.2, 0.06)
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "compare BigDecimals that are not close enough"() {
    expect:
    3.1415 closeTo(3.0, 0.05)
  }

  def "mix integer and floating point numbers"() {
    expect:
    3.1415 closeTo(3, 0.15)
    3 closeTo(3.2, 1)
  }

  @Issue("https://github.com/spockframework/spock/issues/384")
  def "compare infinity"() {
    expect:
    that Float.POSITIVE_INFINITY, closeTo(Float.POSITIVE_INFINITY, 0.1)
    that Double.NEGATIVE_INFINITY, closeTo(Double.NEGATIVE_INFINITY, 0.0)
    that Float.POSITIVE_INFINITY, closeTo(Double.POSITIVE_INFINITY, 0.0)
    that Float.POSITIVE_INFINITY, not(closeTo(Float.NEGATIVE_INFINITY, 99999))
  }

  @Issue("https://github.com/spockframework/spock/issues/384")
  def "compare NaN"() {
    expect:
    that Float.NaN, closeTo(Float.NaN, 0.1)
    that Double.NaN, closeTo(Double.NaN, 0.0)
    that Float.NaN, closeTo(Double.NaN, 0.0)
    that Float.NaN, not(closeTo(9999.9f, 999999999))
  }

  def "error message uses correct values"() {
    when:
    assert that(2.2, closeTo(3.3, 0.5))

    then:
    ConditionNotSatisfiedError e = thrown()
    e.toString().contains("""
Expected: a numeric value within <0.5> of <3.3>
     but: <2.2> differed by <1.1>
    """.trim())
  }
}
