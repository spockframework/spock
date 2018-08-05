/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.mock

import org.spockframework.mock.*
import spock.lang.*

class OrderedInteractions extends Specification {
  def "basic passing example"() {
    def list = Mock(List)

    when:
    list.add(1)
    list.add(2)

    then:
    1 * list.add(1)

    then:
    1 * list.add(2)
  }

  @FailsWith(WrongInvocationOrderError)
  def "basic failing example"() {
    def list = Mock(List)

    when:
    list.add(1)
    list.add(2)

    then:
    1 * list.add(2)

    then:
    1 * list.add(1)
  }

  def "order of invocations belonging to interactions in same then-block is undefined"() {
    def list = Mock(List)

    when:
    list.add(1)
    list.subList(3, 5)
    list.add(2)
    list.clear()

    then:
    2 * list.add(_)
    1 * list.subList(3, 5)

    then:
    1 * list.clear()
  }

  @FailsWith(TooManyInvocationsError)
  def "'too many invocations' wins over 'wrong invocation order'"() {
    def list = Mock(List)

    when:
    list.add("foo")
    list.add("bar")
    list.add("foo")

    then:
    1 * list.add("foo")

    then:
    1 * list.add("bar")
  }

  @Issue("https://github.com/spockframework/spock/issues/475")
  def "mock invocation order works correctly with nested invocations"() {
    given:
    def r1 = Mock(Runnable)
    def r2 = Mock(Runnable)
    def r3 = { r1.run() }

    when:
    r3()

    then:
    1 * r1.run() >> { r2.run() }

    then:
    1 * r2.run()
  }
}

class RemainingBehaviorSameAsForUnorderedInteractions extends Specification {
  @FailsWith(TooFewInvocationsError)
  def "too few invocations for one of the when-blocks"() {
    def list = Mock(List)

    when:
    list.add("foo")
    list.add("bar")

    then:
    m * list.add("foo")
    then:
    n * list.add("bar")

    where:
    m | n
    1 | 2
    2 | 1
  }

  @FailsWith(TooManyInvocationsError)
  def "too many invocations for one of the when-blocks"() {
    def list = Mock(List)

    when:
    list.add("foo")
    list.add("foo")
    list.add("bar")
    list.add("bar")

    then:
    m * list.add("foo")
    then:
    n * list.add("bar")

    where:
    m | n
    1 | 2
    2 | 1
  }
}
