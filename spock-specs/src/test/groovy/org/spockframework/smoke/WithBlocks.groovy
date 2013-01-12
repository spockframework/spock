/*
 * Copyright 2012 the original author or authors.
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
package org.spockframework.smoke

import org.spockframework.runtime.SpockAssertionError
import spock.lang.Specification

class WithBlocks extends Specification {
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

  static class Person {
    String name = "Fred"
    int age = 42
    Person spouse
  }

  static class Employee extends Person {
    String employer = "MarsTravelUnited"
  }

  static class Manager extends Employee {}
}
