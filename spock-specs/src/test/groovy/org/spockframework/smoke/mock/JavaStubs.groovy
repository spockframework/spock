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

import org.spockframework.runtime.InvalidSpecException
import org.spockframework.mock.CannotCreateMockException
import spock.lang.Issue
import spock.lang.Specification
import spock.lang.FailsWith
import spock.mock.MockMakers

class JavaStubs extends Specification {
  def person = Stub(IPerson)

  def "default to empty response"() {
    expect:
    person.name == ""
    person.age == 0
    person.children == []
  }

  def "can be stubbed (using property syntax)"() {
    person.name >> "fred"

    expect:
    person.name == "fred"
  }

  def "can be stubbed (using method syntax)"() {
    person.getName() >> "fred"

    expect:
    person.getName() == "fred"
  }

  @Issue("https://github.com/spockframework/spock/issues/1076")
  //TODO: Parametrize (in separate PR) most of the tests with interface/class based stubs - implementation is not the same
  def "can stub property access"() {
    given:
    person = Stub(Person)

    and:
    person.getName() >> "fred"
    person.age >> 25

    expect:
    person.name == "fred"

    and:
    person.age == 25
  }

  @Issue("https://github.com/spockframework/spock/issues/1076")
  def "can stub property access for implicit getProperty() call"() {
    given:
    person = Stub(Person)

    and:
    person.getName() >> "fred"
    person.age >> 25

    expect:
    person.getProperty("name") == "fred"
    person.getProperty("age") == 25
  }

  def "like to be stubbed at creation time"() {
    person = Stub(IPerson) {
      getName() >> "fred"
    }

    expect:
    person.name == "fred"
  }

  @FailsWith(InvalidSpecException)
  def "cannot be mocked"() {
    1 * person.name >> "fred"

    expect:
    person.name == "fred"
  }

  def "don't match wildcard target"() {
    when:
    person.getName()

    then:
    0 * _.getName()
    0 * _._
    0 * _
  }

  def "can stand in for classes"() {
    Person person = Stub {
      getName() >> "barney"
      getAge() >> 21
      getChildren() >> ["Bamm-Bamm"]
    }

    expect:
    person.name == "barney"
    person.age == 21
    person.children == ["Bamm-Bamm"]
  }

  def "can call real method on class"() {
    def person = Stub(Person, constructorArgs: [])
    person.getName() >> { callRealMethod() }

    expect:
    person.getName() == "default"
  }

  def "can stub final classes"() {
    when:
    def person = Stub(FinalPerson)
    person.phoneNumber >> 6789

    then:
    person.phoneNumber == "6789"
  }

  def "can stub final methods with mockito"() {
    FinalMethodPerson person = Stub(mockMaker: MockMakers.mockito)
    person.phoneNumber >> 6789

    expect:
    person.phoneNumber == "6789"
  }

  def "can stub final methods with mockito with closure"() {
    given:
    FinalMethodPerson person = Stub(mockMaker: MockMakers.mockito) {
      phoneNumber >> 6789
    }

    expect:
    person.phoneNumber == "6789"
  }

  def "can stub final methods with mockito with closure and specified type"() {
    FinalMethodPerson person = Stub(FinalMethodPerson, mockMaker: MockMakers.mockito) {
      phoneNumber >> 6789
    }

    expect:
    person.phoneNumber == "6789"
  }

  def "cannot stub final methods with byteBuddy"() {
    FinalMethodPerson person = Stub(mockMaker: MockMakers.byteBuddy)
    person.phoneNumber >> 6789

    expect:
    person.phoneNumber == "12345"
  }

  def "cannot stub final methods without specifying mockMaker"() {
    FinalMethodPerson person = Stub()
    person.phoneNumber >> 6789

    expect:
    person.phoneNumber == "12345"
  }

  def "cannot stub globally"() {
    when:
    Stub(Person, global: true)

    then:
    CannotCreateMockException e = thrown()
    e.message.contains("global")
  }

  def "no static type specified"() {
    when:
    Stub()

    then:
    InvalidSpecException ex = thrown()
    ex.message == "Mock object type cannot be inferred automatically. Please specify a type explicitly (e.g. 'Mock(Person)')."
  }

  interface IPerson {
    String getName()

    int getAge()

    List<String> getChildren()
  }

  static class Person implements IPerson {
    String name = "default"
    int age
    List<String> children
  }

  static final class FinalPerson extends Person {
    String getPhoneNumber() { "12345" }
  }

  static class FinalMethodPerson extends Person {
    final String getPhoneNumber() { "12345" }
  }
}
