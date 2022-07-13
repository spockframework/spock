/*
 * Copyright 2012 the original author or authors.
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
package org.spockframework.smoke

import org.spockframework.runtime.SpockAssertionError
import spock.lang.*

class WithBlocks extends Specification {
  def "don't turn nested with expressions into condition"() {
    def list = [[['end']]]

    expect:
    with(list) {
      with(it[0]) {
        with(it[0]) {
          with(it[0]) {
            it == 'end'
          }
        }
      }
    }
  }

  def "don't turn nested statements into conditions"() {
    def list = [1, 2]

    expect:
    with(list) {
      list.any {
        it == 2 // would fail if this was turned into a condition
      }
    }
  }

  def "can refer to properties of target object with property syntax"() {
    def person = new Person()

    expect:
    with(person) {
      name == "Fred"
      age == 42
    }
  }

  def "can state the expected target type"() {
    def fred = new Person(spouse: new Manager(name: "Wilma"))

    expect:
    with(fred.spouse, Employee) {
      name == "Wilma"
      employer == "MarsTravelUnited"
    }
  }

  def "fail if expected target type isn't met"() {
    def fred = new Person(spouse: new Person(name: "Wilma"))

    when:
    with(fred.spouse, Employee) {
      name == "Wilma"
      employer == "MarsTravelUnited"
    }

    then:
    SpockAssertionError e = thrown()
    e.message.contains("Employee")
    e.message.contains("Person")
  }

  def "method condition is invoked on closure but not on the spec"() {

    def map = [ 'value1' : 1, 'value2' : 2]

    expect:
    with(map) {
      size() == 2
      containsKey('value2')
    }
  }

  def "nested method conditions are invoked on closure but not on the spec"() {

    def map = [ 'value1' : 1, 'value2' : 2]

    expect:
    with(map) {
      containsKey('value2')

      def list = [1, 2, 3]
      with(list) {
        contains(2)
      }
    }
  }

  def "a closure encloses a with clause that has a method condition"() {
    def list = [1, 2, 3]

    expect:
    (1..3).each { number ->
      with(list) {
        contains(number)
      }
    }
  }

  def "spec has methods with the same signature as the with target object"() {
    def list = [1, 2, 3]

    expect:
    size() == 42        // WithBlocks::size
    contains(4)         // WithBlocks::contains
    with(list) {
      size() == 3       // list::size
      contains(3)       // list::contains
      this.contains(4)  // WithBlocks::size
    }
  }

  @Issue('https://github.com/spockframework/spock/issues/886')
  def "with works with void methods"() {
    given:
    Person person = new Person()
    expect:
    person.check()
    checkCondition()
    with(person) {
      check()
      checkCondition()
      verifyAll {
        check()
        checkCondition()
      }
    }
  }

  int size() {
    42
  }

  boolean contains(Object object) {
    object == 4
  }

  void checkCondition() {
    assert true
  }

  static class Person {
    String name = "Fred"
    int age = 42
    Person spouse

    void check() {
      assert true
    }
  }

  static class Employee extends Person {
    String employer = "MarsTravelUnited"
  }

  static class Manager extends Employee {}
}
