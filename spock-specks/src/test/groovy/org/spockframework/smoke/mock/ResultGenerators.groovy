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

package org.spockframework.smoke.mock

import org.junit.runner.RunWith
import spock.lang.*
import static spock.lang.Predef.*

@Speck
@RunWith(Sputnik)
class ResultGenerators {
  def "return simple result"() {
    List list = Mock()

    when:
    def x0 = list.get(0)
    def x1 = list.get(0)

    then:
    _ * list.get(0) >> 42
    x0 == 42
    x1 == 42
  }

  def "return_calculated_result"() {
    List list = Mock()

    when:
    def x0 = list.get(0)
    def x1 = list.get(0)

    then:
    _ * list.get(0) >> { if(true) 42; else 0 }
    x0 == 42
    x1 == 42
  }

  def "return_closure"() {
    List list = Mock()

    when:
    def x0 = list.get(0)

    then:
    _ * list.get(0) >> (Closure){ if(true) 42; else 0 }
    x0 instanceof Closure
  }

  def "return iterable result"() {
    List list = Mock()

    when:
    def x0 = list.get(0)
    def x1 = list.get(0)
    def x2 = list.get(0)
    def x3 = list.get(0)

    then:
    _ * list.get(0) >>> [0,1,2]
    x0 == 0
    x1 == 1
    x2 == 2
    x3 == 2
  }
}