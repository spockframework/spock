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

package org.spockframework.smoke.mock

import java.util.concurrent.Callable

import spock.lang.*

class ResponseGenerators extends Specification {
  def "return simple response"() {
    List list = Mock()

    when:
    def x0 = list.get(0)
    def x1 = list.get(0)

    then:
    _ * list.get(0) >> 42
    x0 == 42
    x1 == 42
  }

  def "return code response"() {
    List list = Mock()

    when:
    def x0 = list.get(0)
    def x1 = list.get(0)

    then:
    _ * list.get(0) >> { if(true) 42; else 0 }
    x0 == 42
    x1 == 42
  }

  def "return closure"() {
    List list = Mock()

    when:
    def x0 = list.get(0)

    then:
    _ * list.get(0) >> (Closure) { if(true) 42; else 0 }
    x0 instanceof Closure
  }

  def "return iterable response"() {
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

  @Issue("https://github.com/spockframework/spock/issues/205")
  def "auto-coercion of GString return value to String (as in plain Groovy)"() {
    def fred = "Fred"
    def flintstone = "Flintstone"
    def named = Mock(Named)
    named.getName() >> "${fred} ${flintstone}"

    expect:
    named.getName() instanceof String
  }

  @Issue("https://github.com/spockframework/spock/issues/205")
  def "auto-coercion of Integer return value to BigDecimal (as in plain Groovy)"() {
    def calculator = Mock(Calculator)
    calculator.calculate() >> 5

    expect:
    calculator.calculate() instanceof BigDecimal
  }

  @Issue("https://github.com/spockframework/spock/issues/205")
  def "auto-coercion for multi-results"() {
    def calculator = Mock(Calculator)
    calculator.calculate() >>> [1, 2, 3]

    expect:
    calculator.calculate() instanceof BigDecimal
    calculator.calculate() instanceof BigDecimal
    calculator.calculate() instanceof BigDecimal
  }

  @Issue("https://github.com/spockframework/spock/issues/205")
  def "auto-coercion for code responses"() {
    def calculator = Mock(Calculator)
    calculator.calculate() >> { 1 }

    expect:
    calculator.calculate() instanceof BigDecimal
  }

  def "auto-coercion from List to Set"() {
    def producer = Mock(SetProducer)
    producer.produce() >> [1,2,3]

    expect:
    producer.produce() instanceof Set
  }

  def "access args with 'it' variable"() {
    List list = Mock()

    when:
    list.subList(3, 5)

    then:
    1 * list.subList(_, _) >> { assert it[0] == 3; assert it[1] == 5 }
  }

  def "access args with named variable"() {
    List list = Mock()

    when:
    list.subList(3, 5)

    then:
    1 * list.subList(_, _) >> { args -> assert args.size() == 2 }
  }

  def "access args with destructuring"() {
    List list = Mock()

    when:
    list.subList(3, 5)

    then:
    1 * list.subList(_, _) >> { from, to -> assert from == 3; assert to == 5 }
  }

  def "access single arg without destructuring"() {
    List list = Mock()

    when:
    list.remove(3)

    then:
    1 * list.remove(_) >> { foo -> assert foo == [3] }
  }

  def "access single arg with destructuring"() {
    List list = Mock()

    when:
    list.remove(5)

    then:
    1 * list.remove(_) >> { int foo -> assert foo == 5 }
  }

  @Issue("https://github.com/spockframework/spock/issues/288")
  def "exceptions thrown from code response generators aren't wrapped"() {
    def callable = Mock(Callable)
    callable.call() >> { throw exception }

    when:
    def caughtException = caller.call(callable)

    then:
    caughtException.is(exception)

    where:
    [caller, exception] << [
        [new JavaCaller(), new GroovyCaller()],
        [new RuntimeException(), new IOException()]
    ].combinations()
  }

  interface Named {
    String getName()
  }

  interface Calculator {
    BigDecimal calculate()
  }

  interface SetProducer {
    Set produce()
  }

  static class GroovyCaller {
    Throwable call(Callable callable) {
      try {
        callable.call()
        return null
      } catch (Throwable t) {
        return t
      }
    }
  }
}

