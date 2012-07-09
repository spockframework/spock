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

import org.spockframework.runtime.InvalidSpecException

import spock.lang.Specification
import spock.lang.FailsWith
import spock.lang.Ignore

class JavaStubs extends Specification {
  def person = Stub(IPerson)

  def "default to empty response"() {
    expect:
    person.name == ""
    person.age == 0
    person.children == []
  }

  def "can be stubbed"() {
    person.name >> "fred"

    expect:
    person.name == "fred"
  }

  def "are commonly stubbed at creation time"() {
    person = Stub(Person) {
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

  @Ignore("TODO what to do here?")
  def "don't cause problems with wildcard mocking"() {
    when:
    person.getName()

    then:
    //person.name >> "fred"
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
}
