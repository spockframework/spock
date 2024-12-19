/*
 * Copyright 2024 the original author or authors.
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
import spock.lang.Issue
import spock.lang.Specification
import spock.mock.MockMakers

class JavaMocks extends Specification {

  def "can mock final classes"() {
    when:
    def person = Mock(FinalPerson)
    person.phoneNumber >> 6789

    then:
    person.phoneNumber == "6789"
  }

  def "can mock final methods as property with mockito"() {
    FinalMethodPerson person = Mock(mockMaker: MockMakers.mockito)
    person.phoneNumber >> 6789

    expect:
    person.phoneNumber == "6789"
  }

  def "can mock final methods with mockito"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.mockito)
    person.getPhoneNumber() >> 6789

    expect:
    person.getPhoneNumber() == "6789"
  }

  def "can mock final methods with mockito with closure"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.mockito) {
      phoneNumber >> 6789
    }

    expect:
    person.phoneNumber == "6789"
  }

  def "can mock final methods with mockito with closure and specified type"() {
    FinalMethodPerson person = Mock(FinalMethodPerson, mockMaker: MockMakers.mockito) {
      phoneNumber >> 6789
    }

    expect:
    person.phoneNumber == "6789"
  }

  @Issue("https://github.com/spockframework/spock/issues/2039")
  def "cannot mock final methods with byteBuddy"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.byteBuddy)

    when:
    person.getPhoneNumber() >> 6789

    then:
    InvalidSpecException ex = thrown()
    ex.message == "The final method 'getPhoneNumber' of 'person' can't be mocked by the 'byte-buddy' mock maker. Please use another mock maker supporting final methods."

    expect:
    person.getPhoneNumber() == "12345"
  }

  @Issue("https://github.com/spockframework/spock/issues/2039")
  def "cannot mock final methods with byteBuddy without error message when one overload is non final"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.byteBuddy)

    person.finalAndNonFinalOverload() >> "B"

    expect:
    person.finalAndNonFinalOverload() == "final"
  }

  @Issue("https://github.com/spockframework/spock/issues/2039")
  def "non final method overload shall be mockable"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.byteBuddy)

    person.finalAndNonFinalOverload("A") >> "B"

    expect:
    person.finalAndNonFinalOverload("A") == "B"
  }

  def "cannot mock final method as property with byteBuddy"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.byteBuddy)

    when:
    person.phoneNumber >> 6789

    then:
    InvalidSpecException ex = thrown()
    ex.message == "The final method 'getPhoneNumber' of 'person' can't be mocked by the 'byte-buddy' mock maker. Please use another mock maker supporting final methods."

    and:
    person.phoneNumber == "12345"
  }

  def "cannot mock final is getter as property with byteBuddy"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.byteBuddy)

    when:
    person.finalPerson >> false

    then:
    InvalidSpecException ex = thrown()
    ex.message == "The final method 'isFinalPerson' of 'person' can't be mocked by the 'byte-buddy' mock maker. Please use another mock maker supporting final methods."

    expect:
    person.finalPerson
  }

  def "cannot mock final methods without specifying mockMaker"() {
    given:
    FinalMethodPerson person = Mock()

    when:
    person.getPhoneNumber() >> 6789

    then:
    InvalidSpecException ex = thrown()
    ex.message == "The final method 'getPhoneNumber' of 'person' can't be mocked by the 'byte-buddy' mock maker. Please use another mock maker supporting final methods."
  }

  def "no static type specified"() {
    when:
    Mock()

    then:
    InvalidSpecException ex = thrown()
    ex.message == "Mock object type cannot be inferred automatically. Please specify a type explicitly (e.g. 'Mock(Person)')."
  }

  interface IPerson {
    String getName()

    int getAge()
  }

  static class Person implements IPerson {
    String name = "default"
    int age
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  static final class FinalPerson extends Person {
    String getPhoneNumber() { "12345" }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  static class FinalMethodPerson extends Person {

    final String getPhoneNumber() { "12345" }

    final boolean isFinalPerson() { return true }

    final String finalAndNonFinalOverload() {
      return "final"
    }

    String finalAndNonFinalOverload(String arg) {
      return arg
    }
  }
}
