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

import spock.lang.*

class Parameterizations extends Specification {
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
    [_, _] << [["baz", "baz"]]
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

  @Ignore("""argument computer fails with groovy.lang.MissingPropertyException,
  which is expected but cannot be easily tested; should check for this at compile-time""")
  def "arguments may not be used before they have been assigned"() {
    expect: true
    where:
      b = a
      a = 1
  }

  def "simple, multi- and derived parameterizations used together"() {
    expect: d == a * 3
    where:
      a << [1, 2, 3]
      [b, _, c] << [[1, 9, 1], [2, 9, 2], [3, 9, 3]]
      d = a + b + c
  }

  @Ignore("we should either support this or disallow it")
  def "simple parameterization whose value is accessed from closure within other parameterization"() {
    expect:
    a == 1
    b == 1

    where:
    a << 1
    b << {a}()
  }

  @Issue("http://code.google.com/p/spock/issues/detail?id=149")
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
    iterationCount == featureName * 2

    where:
    featureName << [1, 2, 3]
    iterationCount << [2, 4, 6]
  }
}