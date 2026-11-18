/*
 * Copyright 2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.mock

import org.spockframework.mock.CannotCreateMockException
import org.spockframework.runtime.SpockException
import org.spockframework.util.SpockDocLinks
import spock.lang.Issue
import spock.lang.Requires
import spock.lang.Specification

class GroovySpies extends Specification {
  @Issue("https://github.com/spockframework/spock/issues/1815")
  def "can read a missing property through propertyMissing"() {
    given:
    GroovySpyDynamicProperties spy = GroovySpy()
    spy.values.prop1 = "value"

    expect:
    spy.prop1 == "value"
  }

  @Issue("https://github.com/spockframework/spock/issues/1815")
  def "can write a missing property through propertyMissing"() {
    given:
    GroovySpyDynamicProperties spy = GroovySpy()

    when:
    spy.prop1 = "value"

    then:
    spy.values == [prop1: "value"]
  }

  def "can read and write missing properties on a spied instance"() {
    given:
    def spy = GroovySpy(new GroovySpyDynamicProperties())

    when:
    spy.prop1 = "value"

    then:
    spy.prop1 == "value"
  }

  def "can stub missing properties without calling propertyMissing"() {
    given:
    GroovySpyDynamicProperties spy = GroovySpy()

    when:
    def result = spy.prop1
    spy.prop1 = "value"

    then:
    1 * spy.getProp1() >> "stubbed"
    1 * spy.setProp1("value") >> { }
    0 * spy.propertyMissing(_)
    0 * spy.propertyMissing(_, _)
    result == "stubbed"
    spy.values.isEmpty()
  }

  def "can call the real propertyMissing from an interaction"() {
    given:
    GroovySpyDynamicProperties spy = GroovySpy()

    when:
    spy.prop1 = "value"
    def result = spy.prop1

    then:
    1 * spy.setProp1("value") >> { callRealMethod() }
    1 * spy.getProp1() >> { callRealMethod() }
    result == "value"
    spy.values == [prop1: "value"]
  }

  def "can spy on concrete instances"() {
    def person = GroovySpy(new Person())

    when:
    def result = person.work()

    then:
    1 * person.work()
    result == "singing, singing"
  }

  def "does not retry a final setter that throws"() {
    given:
    ThrowingPropertyAccessors spy = GroovySpy()

    when:
    spy.value = "value"

    then:
    IOException error = thrown()
    error.message == "setter failed"
    spy.setterCalls == 1
  }

  def "does not retry a final getter that throws"() {
    given:
    ThrowingPropertyAccessors spy = GroovySpy()

    when:
    spy.value

    then:
    IOException error = thrown()
    error.message == "getter failed"
    spy.getterCalls == 1
  }

  def "using >>_ does not call original method and produces stubbed value"() {
    def person = GroovySpy(new Person())

    when:
    def result = person.formattedAge

    then:
    1 * person.getFormattedAge()
    result == "42 years old"

    when:
    def result2 = person.formattedAge

    then:
    1 * person.getFormattedAge() >> _
    result2 == ""
  }

  def "spying on concrete instances can use partial mocking"() {
    def person = GroovySpy(new Person())

    when:
    def result = person.work()

    then:
    1 * person.work()
    1 * person.getTask() >> "work"
    result == "work, work"
  }

  def "can spy on instances of classes with no default constructor"() {
    given:
    def spy = GroovySpy(new NoDefaultConstructor(42))

    expect:
    spy.value == 42

    when:
    def result = spy.value

    then:
    1 * spy.getValue() >> 7
    result == 7
  }

  def "can define interactions for spy on instances directly with closure"() {
    given:
    def spy = GroovySpy(new NoDefaultConstructor(42)) {
      1 * value >> 7
    }

    expect:
    spy.value == 7
  }

  def "inferred type is ignored for instance mocks"() {
    when:
    Being person = new Person()
    Being second = GroovySpy(person)

    then:
    second instanceof Person
    noExceptionThrown()
  }

  def "do not allow spying on other mocks"() {
    given:
    ArrayList src = GroovySpy()

    when:
    def other = GroovySpy(src)

    then:
    thrown(SpockException)
  }

  @Requires(
    value = { jvm.java17Compatible },
    reason = "Only happens on 17+, without an explicit --add-opens"
  )
  def "known issue copy fields"() {
    when:
    List second = GroovySpy(new ArrayList())

    then:
    CannotCreateMockException e = thrown()
    e.message == "Cannot create mock for class java.util.ArrayList. Cannot copy fields.\n" +
      SpockDocLinks.SPY_ON_JAVA_17.link
  }

  interface Being {
  }

  static class Person implements Being {
    String name
    int age

    Person() {
      this("fred", 42)
    }

    Person(String name, int age) {
      this.name = name
      this.age = age
    }

    def work() {
      ([task] * workHours).join(", ")
    }

    def getTask() {
      "singing"
    }

    def getWorkHours() {
      2
    }

    String getFormattedAge() {
      "$age years old"
    }

    String toString() {
      "Hi, I'm $name"
    }

    boolean equals(Object other) {
      other instanceof Person && name == other.name
    }

    int hashCode() {
      name.hashCode()
    }
  }

  static class NoDefaultConstructor {
    int value

    NoDefaultConstructor(int value) {
      this.value = value
    }
  }

  static class ThrowingPropertyAccessors {
    int setterCalls
    int getterCalls

    final void setValue(String value) throws IOException {
      setterCalls++
      throw new IOException("setter failed")
    }

    final String getValue() throws IOException {
      getterCalls++
      throw new IOException("getter failed")
    }
  }
}

class GroovySpyDynamicProperties {
  Map<String, Object> values = [:]

  Object propertyMissing(String name) {
    values[name]
  }

  void propertyMissing(String name, Object value) {
    values[name] = value
  }
}
