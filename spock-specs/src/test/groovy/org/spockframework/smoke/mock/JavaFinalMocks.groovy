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

import spock.lang.FailsWith

import org.spockframework.runtime.InvalidSpecException
import spock.lang.Issue
import spock.lang.Specification
import spock.mock.MockMakers

class JavaFinalMocks extends Specification {

  def "can mock final classes"() {
    given:
    def person = Mock(FinalPerson)

    when:
    def result = person.phoneNumber

    then:
    1 * person.phoneNumber >> 6789
    result == "6789"
  }

  def "can mock final methods as property with mockito"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.mockito)

    when:
    def result = person.phoneNumber

    then:
    1 * person.phoneNumber >> 6789
    result == "6789"
  }

  def "can mock final methods with mockito"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.mockito)
    person.getPhoneNumber() >> 6789

    when:
    def result = person.getPhoneNumber()

    then:
    1 * person.getPhoneNumber() >> 6789
    result == "6789"
  }

  def "can mock final methods with mockito with closure"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.mockito) {
     1 * phoneNumber >> 6789
    }

    expect:
    person.phoneNumber == "6789"
  }

  def "can mock final methods with mockito with closure and specified type"() {
    given:
    FinalMethodPerson person = Mock(FinalMethodPerson, mockMaker: MockMakers.mockito) {
     1 * phoneNumber >> 6789
    }

    expect:
    person.phoneNumber == "6789"
  }

  @Issue("https://github.com/spockframework/spock/issues/2039")
  @FailsWith(
      value = InvalidSpecException,
      expectedMessage = "The final method 'getPhoneNumber' of 'person' can't be mocked by the 'byte-buddy' mock maker. Please use another mock maker supporting final methods."
  )
  def "cannot mock final methods with byteBuddy"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.byteBuddy)

    when:
    person.getPhoneNumber()

    then:
    1 * person.getPhoneNumber() >> 6789
  }


  @FailsWith(
      value = InvalidSpecException,
      expectedMessage = "The final method 'getPhoneNumber' of 'person' can't be mocked by the 'byte-buddy' mock maker. Please use another mock maker supporting final methods."
  )
  def "cannot mock final method as property with byteBuddy"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.byteBuddy)

    when:
    person.phoneNumber

    then:
    1 * person.phoneNumber >> 6789
  }

  @FailsWith(
      value = InvalidSpecException,
      expectedMessage = "The final method 'isFinalPerson' of 'person' can't be mocked by the 'byte-buddy' mock maker. Please use another mock maker supporting final methods."
  )
  def "cannot mock final is getter as property with byteBuddy"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.byteBuddy)

    when:
    person.finalPerson

    then:
    1 * person.finalPerson >> false
  }

  @Issue("https://github.com/spockframework/spock/issues/2039")
  def "cannot mock final methods with byteBuddy when one overload is non final no error message is produced"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.byteBuddy)

    when: "calling the method that has both a final and non final overload"
    def result = person.finalAndNonFinalOverload()

    then: "the mocking does not work"
    0 * person.finalAndNonFinalOverload() >> "B"

    and: "the result is not stubbed"
    result == "final"
  }

  @Issue("https://github.com/spockframework/spock/issues/2039")
  def "non final method overload shall be mockable"() {
    given:
    FinalMethodPerson person = Mock(mockMaker: MockMakers.byteBuddy)

    when:
    def result = person.finalAndNonFinalOverload("A")

    then:
    person.finalAndNonFinalOverload("A") >> "B"
    result == "B"
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
