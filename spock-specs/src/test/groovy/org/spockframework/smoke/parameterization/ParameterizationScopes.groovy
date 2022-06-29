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

import spock.lang.Issue
import spock.lang.Specification

class ParameterizationScopes extends Specification {
  def "in outer scope"() {
    expect: x == 1
    where: x << 1
  }

  def "in inner scope"() {
    def sum = 0

    when:
    for (i in 1..3) {
      sum += inc
    }

    then:
    sum == 6

    where:
    inc << 2
  }

  def "in closure scope"() {
    def sum = 0

    when:
    (1..3).each {
      sum += inc
    }

    then:
    sum == 6

    where:
    inc << 2
  }

  @Issue("https://github.com/spockframework/spock/issues/185")
  def "in nested closure scope"() {
    expect:
    1.every {
      1.every {
        { -> val }() == 1
      }
    }

    where:
    val = 1
  }

  @Issue("https://github.com/spockframework/spock/issues/408")
  def "in closure scope of derived data value"() {
    expect:
    val2 == [2, 3]

    where:
    val1 = 2
    val2 = [1, 2, 3].findAll { it >= val1 }
  }

  @Issue("https://github.com/spockframework/spock/issues/408")
  def "in nested closure scope of derived data value"() {
    expect:
    val2 == [2, 3]

    where:
    val1 = 2
    val2 = [1, 2, 3].findAll { x ->
      def val1Copy = val1
      (0..2).every {
        x >= val1 && x >= val1Copy
      }
    }
  }
}
