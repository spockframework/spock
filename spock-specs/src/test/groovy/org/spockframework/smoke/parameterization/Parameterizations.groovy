/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.smoke.parameterization

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.*

@Rollup
class Parameterizations extends EmbeddedSpecification {
  def "multi-parameterization"() {
    expect: a == b
    where:
      [a, b] << [[1, 1], [2, 2], [3, 3]]
  }

  def "multi-derived-parameterization"() {
    expect:
    runner.runFeatureBody '''
expect: a == b
where :
  aAndB << [[1, 1], [2, 2], [3, 3]]
  (a, b) = aAndB
'''
  }

  def "multi-parameterization with named parameters"() {
    expect:
    a == b
    where:
    [a, b] << [[a: 1, b: 1], [a: 2, b: 2], [b: 3, a: 3]]
  }


  def "multi-parameterization with placeholder in first position"() {
    expect: a == b
    where:
      [_, a, b] << [[9, 1, 1], [9, 2, 2], [9, 3, 3]]
  }

  def "multi-derived-parameterization with placeholder in first position"() {
    expect: a == b
    where :
      aAndB << [[9, 1, 1], [9, 2, 2], [9, 3, 3]]
      (_, a, b) = aAndB
  }

  def "multi-parameterization with placeholder in second position"() {
    expect: a == b
    where:
      [a, _, b] << [[1, 9, 1], [2, 9, 2], [3, 9, 3]]
  }

  def "multi-derived-parameterization with placeholder in second position"() {
    expect: a == b
    where :
      aAndB << [[1, 9, 1], [2, 9, 2], [3, 9, 3]]
      (a, _, b) = aAndB
  }

  def "multi-parameterization with placeholder in last position"() {
    expect: a == b
    where:
      [a, b, _] << [[1, 1, 9], [2, 2, 9], [3, 3, 9]]
  }

  def "multi-derived-parameterization with placeholder in last position"() {
    expect: a == b
    where :
      aAndB << [[1, 1, 9], [2, 2, 9], [3, 3, 9]]
      (a, b, _) = aAndB
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "multi-parameterization with placeholder in wrong position"() {
    expect: a == b
    where:
      [a, b, _] << [[1, 9, 1]]
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "multi-derived-parameterization with placeholder in wrong position"() {
    expect: a == b
    where :
      aAndB << [[1, 9, 1]]
      (a, b, _) = aAndB
  }

  def "multi-parameterization with multiple placeholders"() {
    expect:
    a == b

    where:
    [_, a] << [["foo", 3]]
    [b, _] << [[3, "bar"]]
  }

  def "multi-derived-parameterization with multiple placeholders"() {
    expect:
    a == b

    where:
    fooAndA << [["foo", 3]]
    (_, a) = fooAndA
    bAndBar << [[3, "bar"]]
    (b, _) = bAndBar
  }

  def "nested multi-parameterization"() {
    expect:
    runner.runFeatureBody '''
expect: a == b
where:
  [a, [_, b]] << [[3, [1, 3]]]
'''
  }

  def "nested multi-parameterization with named parameters"() {
    expect:
    runner.runFeatureBody '''
expect: a == b
where:
  [a, [_, b]] << [[3, [c:1, b:3]]]
'''
  }

  def "derived parameterization"() {
    expect: a == b.toUpperCase()
    where:
      a << ["A", "B", "C"]
      b = a.toLowerCase()
  }

  def "dependent derived parameterizations"() {
    expect: d == a
    where:
      a << [1, 2, 3]
      b = a
      c = b * 2
      d = c / 2
  }

  def 'arguments may not be used before they have been assigned'() {
    when:
    runner.runFeatureBody '''
expect: true
where:
  b = a
  a = 1
    '''

    then:
    thrown Exception
  }

  def "simple, multi-, derived and multi-derived parameterizations used together"() {
    expect:
      d == a * 3
      e == a
      f == a
    where:
      a << [1, 2, 3]
      [b, _, c] << [[1, 9, 1], [2, 9, 2], [3, 9, 3]]
      d = a + b + c
      (e, _, f) = [a, b, c]
  }

  @Issue("https://github.com/spockframework/spock/issues/271")
  def "can use data variables named p0, p1, etc."() {
    expect:
    p1 == p0 * 2

    where:
    p0 | p1
    1  | 2
    2  | 4
    3  | 6
  }

  def "can use data variables named like special @Unroll variables"() {
    expect:
    iterationIndex == featureName * 2

    where:
    featureName << [1, 2, 3]
    iterationIndex << [2, 4, 6]
  }

  @Issue("https://github.com/spockframework/spock/issues/396")
  def "can call closures contained in data variables with method syntax"() {
    expect:
    a() == 1
    b(1, 2) == 3
    1.times { n ->
      assert b(n, n) - n == n
    }

    and:
    "123".size() == 3

    where:
    a = { 1 }
    b = { i, j -> i + j }
    size = { 42 }
  }

  @Issue("https://github.com/spockframework/spock/issues/880")
  def "variables in helper methods do not interfere with omitted data variables"() {
    expect:
    runner.runSpecBody '''
def "#a <=> #b: #result"() {
  expect:
  Math.signum(a <=> b) == result

  where:
  a          | b          | result
  "abcdef12" | "abcdef12" | 0
}

private double[] someOtherMethod() {
  double[] result = new double[0]
  return result
}
'''
  }

  @Issue("https://github.com/spockframework/spock/issues/880")
  def "variables in helper methods do not interfere with typed data variables"() {
    expect:
    runner.runSpecBody '''
def "roll #x"(Integer x) {
    expect:
    1 == x

    where:
    x << [1]
}

private int unused() {
    String x = "4"
    return 3
}
'''
  }
}
