/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.parameterization

import org.spockframework.EmbeddedSpecification
import spock.lang.*

class Parameterizations extends EmbeddedSpecification {
  def "multi-parameterization"() {
    expect: a == b
    where :
      [a, b] << [[1, 1], [2, 2], [3, 3]]
  }

  def "multi-parameterization with placeholder in first position"() {
    expect: a == b
    where :
      [_, a, b] << [[9, 1, 1], [9, 2, 2], [9, 3, 3]]
  }

  def "multi-parameterization with placeholder in second position"() {
    expect: a == b
    where :
      [a, _, b] << [[1, 9, 1], [2, 9, 2], [3, 9, 3]]
  }

  def "multi-parameterization with placeholder in last position"() {
    expect: a == b
    where :
      [a, b, _] << [[1, 1, 9], [2, 2, 9], [3, 3, 9]]
  }

  @FailsWith(org.spockframework.runtime.ConditionNotSatisfiedError)
  def "multi-parameterization with placeholder in wrong position"() {
    expect: a == b
    where :
      [a, b, _] << [[1, 9, 1]]
  }

  def "multi-parameterization with multiple placeholders"() {
    expect:
    a == b

    where:
    [_, a] << [["foo", 3]]
    [b, _] << [[3, "bar"]]
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

  def "simple, multi- and derived parameterizations used together"() {
    expect: d == a * 3
    where:
      a << [1, 2, 3]
      [b, _, c] << [[1, 9, 1], [2, 9, 2], [3, 9, 3]]
      d = a + b + c
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
}
