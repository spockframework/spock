/*
 * Copyright 2012 the original author or authors.
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

import spock.lang.ConcurrentExecutionMode

import java.lang.reflect.Modifier

import spock.lang.Specification

class GroovySpiesThatAreGlobal extends Specification {
  def "mock instance method"() {
    def myList = new ArrayList()
    def anyList = GroovySpy(ArrayList, global: true)

    when:
    myList.add(1)
    myList.size()

    then:
    1 * anyList.add(1)
    1 * anyList.size()
    0 * _
  }

  def "global mocking is undone before next method"() {
    when:
    new ArrayList().add(1)

    then:
    0 * _
  }

  def "calls real method if call isn't mocked"() {
    def myList = new ArrayList()
    GroovySpy(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    myList.size() == 2
  }

  def "calls real method if mocked call provides no result"() {
    def myList = new ArrayList()
    GroovySpy(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    2 * myList.add(1)
    myList.size() == 2
  }

  def "does not call real method if mocked call provides result"() {
    def myList = new ArrayList()
    GroovySpy(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    2 * myList.add(1) >> true
    myList.size() == 0
  }

  def "can call real method when providing result"() {
    def myList = new ArrayList()
    GroovySpy(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    2 * myList.add(1) >> { callRealMethod() }
    myList.size() == 2
  }

  def "can call real method with changed arguments"() {
    def myList = new ArrayList()
    GroovySpy(ArrayList, global: true)

    when:
    myList.add(1)
    myList.add(1)

    then:
    2 * myList.add(1) >> { callRealMethodWithArgs(42) }
    myList.size() == 2
    myList[1] == 42
  }

  def "mock dynamic instance method"() {
    def anyList = GroovySpy(ArrayList, global: true)

    when:
    new ArrayList().foo(42)

    then:
    1 * anyList.foo(42) >> true
  }

  def "mock dynamic instance method called via MOP"() {
    def anyPerson = GroovySpy(Person, global: true)

    when:
    new Person().invokeMethod("foo", [42] as Object[])

    then:
    1 * anyPerson.foo(42) >> "done"
  }

  def "mock dynamic property getter called via MOP"() {
    def anyPerson = GroovySpy(Person, global: true)

    when:
    new Person().getProperty("foo")

    then:
    1 * anyPerson.foo >> "done"
  }

  def "mock dynamic property setter called via MOP"() {
    def anyPerson = GroovySpy(Person, global: true)

    when:
    new Person().setProperty("foo", 42)

    then:
    1 * anyPerson.setFoo(42) >> "done"
  }

  def "mock final instance method"() {
    assert Modifier.isFinal(Person.getMethod("performFinal", String).getModifiers())

    def anyPerson = GroovySpy(Person, global: true)

    when:
    new Person().performFinal("work")

    then:
    1 * anyPerson.performFinal("work")
  }

  def "mock final class"() {
    assert Modifier.isFinal(FinalPerson.getModifiers())

    def anyPerson = GroovySpy(FinalPerson, global: true)

    when:
    new FinalPerson().perform("work")

    then:
    1 * anyPerson.perform("work")
  }

  def "mock static method"() {
    GroovySpy(Collections, global: true)

    when:
    Collections.emptyList()
    Collections.nCopies(42, "elem")

    then:
    1 * Collections.emptyList()
    1 * Collections.nCopies(42, "elem")
    0 * _
  }

  def "mock dynamic static method"() {
    GroovySpy(Collections, global: true)

    when:
    Collections.foo()
    Collections.bar(42, "elem")

    then:
    1 * Collections.foo() >> 0
    1 * Collections.bar(42, "elem") >> 0
    0 * _
  }

  def "mock constructor"() {
    GroovySpy(Person, global: true)

    when:
    new Person("fred", 42)
    new Person("barney", 21)

    then:
    1 * new Person("fred", 42)
    1 * new Person("barney", 21)
    0 * _
  }

  static class Person {
    String name
    int age

    String perform(String work) { "done" }
    final String performFinal(String work) { "done" }

    Person(String name = "fred", int age = 42) {
      this.name = name
      this.age = age
    }
  }

  static final class FinalPerson {
    String name
    int age

    String perform(String work) { "done" }

    FinalPerson(String name = "fred", int age = 42) {
      this.name = name
      this.age = age
    }
  }
}
