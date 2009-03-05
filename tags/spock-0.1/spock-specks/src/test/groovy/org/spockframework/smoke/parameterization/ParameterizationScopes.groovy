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

import org.spockframework.*
import org.junit.runner.RunWith
import spock.lang.Sputnik
import spock.lang.Speck
import spock.lang.Sputnik

@Speck
@RunWith(Sputnik)
class ParameterizationScopes {
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
}