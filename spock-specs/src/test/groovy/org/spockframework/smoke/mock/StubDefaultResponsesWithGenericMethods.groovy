/*
 * Copyright 2013 the original author or authors.
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

import spock.lang.Specification

class StubDefaultResponsesWithGenericMethods extends Specification {
  interface Person<T> {
    T getPet()
  }

  static class Cat {}

  static class CatPerson<T> implements Person<Cat> {
    Cat getPet() { new Cat() }

    T getGadget() { null }
  }

  static class Phone {}

  static class PhonePerson extends CatPerson<Phone> {}

  def "class that implements parameterized interface"() {
    def person = Stub(CatPerson)

    expect:
    person.pet instanceof Cat
  }

  def "class that extends parameterized class"() {
    def person = Stub(PhonePerson)

    expect:
    person.gadget instanceof Phone
  }

  interface PersonHolder {
    Person<Cat> getPerson()
  }

  def "interface with parameterized return type"() {
    def holder = Stub(PersonHolder)

    expect:
    holder.person.pet instanceof Cat
  }
}
