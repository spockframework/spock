/*
 * Copyright 2012 the original author or authors.
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
import org.spockframework.runtime.InvalidSpecException
import org.spockframework.runtime.SpockException
import org.spockframework.util.SpockDocLinks
import spock.lang.*
import spock.mock.MockMakers

class JavaSpies extends Specification {
  def "construct spied-on object using default constructor when no constructor args given (even if Objenesis is available on class path)"() {
    when:
    Spy(NoDefaultConstructor)

    then:
    thrown(CannotCreateMockException)
  }

  def "construct spied-on object using provided constructor args"() {
    given:
    Constructable spy = Spy(constructorArgs: ctorArgs)

    expect:
    spy.arg1 == arg1
    spy.arg2 == arg2
    spy.arg3 == arg3
    spy.arg4 == arg4

    where:
    ctorArgs                         | arg1 | arg2 | arg3 | arg4
    [1]                              | 1    | 0    | 0    | null
    [2, 3]                           | 0    | 2    | 3    | null
    ["hi"]                           | 0    | 0    | 0    | "hi"
    [arg4: "hi"]                     | 0    | 0    | 0    | "hi"
    [arg1: 2, arg3: 5, arg4: "hi"]   | 2    | 0    | 5    | "hi"
    [[arg1: 2, arg3: 5, arg4: "hi"]] | 2    | 0    | 5    | "hi"
    [arg2: 1]                        | 0    | 1    | 0    | null
  }

  def "call real methods by default"() {
    given:
    Person person = Spy(constructorArgs: ["fred", 42])

    expect:
    person.name == "fred"
    person.age == 42
  }

  def "call real equals method by default"() {
    given:
    Person fred1 = Spy(constructorArgs: ["fred", 42])
    Person fred2 = Spy(constructorArgs: ["fred", 21])
    Person barney = Spy(constructorArgs: ["barney", 33])

    expect:
    fred1 == fred2
    fred1 != barney
  }

  def "call real hashCode method by default"() {
    given:
    Person person = Spy(constructorArgs: ["fred", 42])

    expect:
    person.hashCode() == "fred".hashCode()
  }

  def "call real toString method by default"() {
    given:
    Person person = Spy(constructorArgs: ["fred", 42])

    expect:
    person.toString() == "Hi, I'm fred"
  }

  def "can verify interactions with real methods"() {
    given:
    Person person = Spy(constructorArgs: ["fred", 42])

    when:
    def result = person.work()

    then:
    1 * person.work()
    1 * person.getTask()
    1 * person.getWorkHours()
    result == "singing, singing"
  }

  def "can be used as partial mocks"() {
    given:
    def person = Spy(Person, constructorArgs: ["fred", 42]) {
      getWorkHours() >>> [3, 2, 1]
    }

    expect:
    person.work() == "singing, singing, singing"
    person.work() == "singing, singing"
    person.work() == "singing"
  }

  def "can spy on concrete instances"() {
    given:
    def person = Spy(new Person())

    when:
    def result = person.work()

    then:
    1 * person.work()
    result == "singing, singing"
  }

  @Issue("https://github.com/spockframework/spock/issues/1035")
  def "using >>_ does not call original method and produces stubbed value"() {
    given:
    def person = Spy(new Person())

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

  @Issue("https://github.com/spockframework/spock/issues/771")
  def "spying on concrete instances can use partial mocking"() {
    given:
    def person = Spy(new Person())

    when:
    def result = person.work()

    then:
    1 * person.work()
    1 * person.getTask() >> "work"
    result == "work, work"
  }

  @Issue("https://github.com/spockframework/spock/issues/769")
  def "can spy on instances of classes with no default constructor"() {
    given:
    def spy = Spy(new NoDefaultConstructor(42))

    expect:
    spy.value == 42

    when:
    def result = spy.value

    then:
    1 * spy.getValue() >> 7
    result == 7
  }

  @Issue("https://github.com/spockframework/spock/issues/1028")
  def "can define interactions for spy on instances directly with closure"() {
    given:
    def spy = Spy(new NoDefaultConstructor(42)) {
      1 * value >> 7
    }

    expect:
    spy.value == 7
  }

  def "can stub final classes"() {
    when:
    FinalPerson person = Spy()
    person.phoneNumber >> 6789

    then:
    person.phoneNumber == "6789"
  }

  def "can spy final methods as property with mockito"() {
    given:
    FinalMethodPerson person = Spy(mockMaker: MockMakers.mockito)
    person.phoneNumber >> 6789

    expect:
    person.phoneNumber == "6789"
  }

  def "can spy final methods with mockito"() {
    given:
    FinalMethodPerson person = Spy(mockMaker: MockMakers.mockito)
    person.getPhoneNumber() >> 6789

    expect:
    person.getPhoneNumber() == "6789"
  }

  def "can spy final methods with mockito with closure"() {
    given:
    FinalMethodPerson person = Spy(mockMaker: MockMakers.mockito) {
      phoneNumber >> 6789
    }

    expect:
    person.phoneNumber == "6789"
  }

  @Issue("https://github.com/spockframework/spock/issues/2039")
  def "cannot spy on final methods with byteBuddy"() {
    given:
    FinalMethodPerson person = Spy(mockMaker: MockMakers.byteBuddy)

    when:
    person.getPhoneNumber() >> 6789

    then:
    InvalidSpecException ex = thrown()
    ex.message == "The final method 'getPhoneNumber' of 'person' can't be mocked by the 'byte-buddy' mock maker. Please use another mock maker supporting final methods."

    expect:
    person.getPhoneNumber() == "12345"
  }

  @Issue("https://github.com/spockframework/spock/issues/2039")
  def "cannot spy on final methods as property with byteBuddy"() {
    given:
    FinalMethodPerson person = Spy(mockMaker: MockMakers.byteBuddy)

    when:
    person.phoneNumber >> 6789

    then:
    InvalidSpecException ex = thrown()
    ex.message == "The final method 'getPhoneNumber' of 'person' can't be mocked by the 'byte-buddy' mock maker. Please use another mock maker supporting final methods."

    expect:
    person.phoneNumber == "12345"
  }

  def "cannot spy on final methods without specifying mockMaker"() {
    given:
    FinalMethodPerson person = Spy()

    when:
    person.phoneNumber >> 6789

    then:
    InvalidSpecException ex = thrown()
    ex.message == "The final method 'getPhoneNumber' of 'person' can't be mocked by the 'byte-buddy' mock maker. Please use another mock maker supporting final methods."
  }

  def "cannot spy globally"() {
    when:
    Spy(Person, global: true)

    then:
    CannotCreateMockException e = thrown()
    e.message.contains("global")
  }

  @Issue("https://github.com/spockframework/spock/issues/822")
  def "inferred type is ignored for instance mocks"() {
    when:
    Being person = new Person()
    Being second = Spy(person)

    then:
    second instanceof Person
    noExceptionThrown()
  }

  @Issue("https://github.com/spockframework/spock/issues/1029")
  def "do not allow spying on other mocks"() {
    given:
    ArrayList src = Spy()

    when:
    def other = Spy(src)

    then:
    thrown(SpockException)
  }

  @Requires(
    value = { jvm.java17Compatible },
    reason = "Only happens on 17+, without an explicit --add-opens"
  )
  def "known issue copy fields"() {
    when:
    List second = Spy(new ArrayList())

    then:
    CannotCreateMockException e = thrown()
    e.message == "Cannot create mock for class java.util.ArrayList. Cannot copy fields.\n" +
      SpockDocLinks.SPY_ON_JAVA_17.link
  }

  def "no static type specified"() {
    when:
    Spy()

    then:
    InvalidSpecException ex = thrown()
    ex.message == "Spy object type cannot be inferred automatically. Please specify a type explicitly (e.g. 'Spy(Person)')."
  }

  def "specified instance is null"() {
    when:
    Spy((Object) null)

    then:
    SpockException ex = thrown()
    ex.message == "Spy instance may not be null"
  }

  static class Constructable {
    int arg1
    int arg2
    int arg3
    String arg4

    Constructable() {
    }

    Constructable(int arg1) {
      this.arg1 = arg1
    }

    Constructable(int arg2, int arg3) {
      this.arg2 = arg2
      this.arg3 = arg3
    }

    Constructable(String arg4) {
      this.arg4 = arg4
    }
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

  static final class FinalPerson extends Person {
    String getPhoneNumber() { "12345" }
  }

  static class FinalMethodPerson extends Person {
    final String getPhoneNumber() { "12345" }
  }

  static class NoDefaultConstructor {
    int value
    NoDefaultConstructor(int value) {
      this.value = value
    }
  }
}
