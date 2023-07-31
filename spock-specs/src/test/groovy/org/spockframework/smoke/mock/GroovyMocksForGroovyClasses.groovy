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

import org.spockframework.mock.*
import spock.lang.*

class GroovyMocksForGroovyClasses extends Specification {
  def person = GroovyMock(Person)

  def "physical method"() {
    when:
    person.sing("song")

    then:
    1 * person.sing("song")
  }

  @Issue("https://github.com/spockframework/spock/issues/1216")
  def "argument coercion"() {
    when:
    person.sing("song${1}")

    then:
    1 * person.sing('song1')
  }

  def "final physical method"() {
    def finalPerson = GroovyMock(clazz)

    when:
    finalPerson.sing("song")

    then:
    1 * finalPerson.sing("song")

    where:
    clazz << [FinalPerson, FinalMethodsPerson, FinalJavaPerson, FinalMethodsJavaPerson]
  }

  def "dynamic method"() {
    when:
    person.greet("henry")

    then:
    1 * person.greet("henry")
  }

  def "DGM method"() {
    when:
    person.dump()

    then:
    1 * person.dump()
  }

  def "physical method via GroovyObject.invokeMethod"() {
    when:
    person.invokeMethod("sing", ["song"] as Object[])

    then:
    1 * person.sing("song")
  }

  def "dynamic method via GroovyObject.invokeMethod"() {
    when:
    person.invokeMethod("greet", ["henry", "freddy"] as Object[])

    then:
    1 * person.greet("henry", "freddy")
  }

  def "DGM method via GroovyObject.invokeMethod"() {
    when:
    person.invokeMethod("dump", [] as Object[])

    then:
    1 * person.dump()
  }

  def "get physical property"() {
    when:
    person.name

    then:
    1 * person.getName()

    when:
    person.name

    then:
    1 * person.name
  }

  def "get final physical property"() {
    def finalPerson = GroovyMock(clazz)

    when:
    finalPerson.name

    then:
    1 * finalPerson.getName()

    when:
    finalPerson.name

    then:
    1 * finalPerson.name

    where:
    clazz << [FinalPerson, FinalMethodsPerson, FinalJavaPerson, FinalMethodsJavaPerson]
  }

  def "get dynamic property"() {
    when:
    person.age

    then:
    1 * person.getAge()

    when:
    person.age

    then:
    1 * person.age
  }

  def "get DGM property"() {
    when:
    person.properties

    then:
    1 * person.getProperties()

    when:
    person.properties

    then:
    1 * person.properties
  }

  def "get physical property via GroovyObject.getProperty"() {
    when:
    person.getProperty("name")

    then:
    1 * person.getName()

    when:
    person.getProperty("name")

    then:
    1 * person.name
  }

  def "get dynamic property via GroovyObject.getProperty"() {
    when:
    person.getProperty("age")

    then:
    1 * person.getAge()

    when:
    person.getProperty("age")

    then:
    1 * person.age
  }

  def "get DGM property via GroovyObject.getProperty"() {
    when:
    person.getProperty("properties")

    then:
    1 * person.getProperties()

    when:
    person.getProperty("properties")

    then:
    1 * person.properties
  }

  def "set physical property"() {
    when:
    person.name = "fred"

    then:
    1 * person.setName("fred")
  }

  def "set dynamic property"() {
    when:
    person.age = 42

    then:
    1 * person.setAge(42)
  }

  def "set DGM property"() {
    when:
    person.metaClass = null

    then:
    1 * person.setMetaClass(null)
  }

  def "set physical property via GroovyObject.setProperty"() {
    when:
    person.setProperty("name", "fred")

    then:
    1 * person.setName("fred")
  }

  def "set dynamic property via GroovyObject.setProperty"() {
    when:
    person.setProperty("age", 42)

    then:
    1 * person.setAge(42)
  }

  def "set DGM property via GroovyObject.setProperty"() {
    when:
    person.setProperty("metaClass", null)

    then:
    1 * person.setMetaClass(null)
  }

  @Issue("https://github.com/spockframework/spock/issues/1270")
  def "Mock object boolean (is) accessor via dot-notation" () {
    given:
    ExampleData mockData = GroovyMock(ExampleData)

    when: "query via property syntax"
    def result = mockData.current ? "Data is current" : "Data is not current"

    then: "calls mock"
    1 * mockData.isCurrent() >> true

    and:
    result == "Data is current"
  }

  @Issue("https://github.com/spockframework/spock/issues/1270")
  def "Mock object boolean (get) accessor via dot-notation" () {
    given:
    ExampleData mockData = GroovyMock(ExampleData)

    when: "query via property syntax"
    def result = mockData.enabled ? "Data is current" : "Data is not current"

    then: "calls mock"
    1 * mockData.getEnabled() >> true

    and:
    result == "Data is current"
  }

  @Issue("https://github.com/spockframework/spock/issues/1270")
  def "Mock object boolean (get + is) accessor via dot-notation" () {
    given:
    ExampleData mockData = GroovyMock(ExampleData)

    when: "query via property syntax"
    def result = mockData.active ? "Data is current" : "Data is not current"

    then: "calls mock, preferring 'is' to 'get' for boolean getters"
    1 * mockData.isActive() >> true
    0 * mockData.getActive()

    and:
    result == "Data is current"
  }

  @Issue("https://github.com/spockframework/spock/issues/1270")
  def "Mock object non-boolean (get + is) accessor via dot-notation" () {
    given:
    ExampleData mockData = GroovyMock(ExampleData)

    when: "query via property syntax"
    def result = mockData.name ? "Data is current" : "Data is not current"

    then: "calls mock, preferring 'get' to 'is' for non-boolean getters"
    1 * mockData.getName() >> "X"
    0 * mockData.isName()

    and:
    result == "Data is current"
  }

  @Issue("https://github.com/spockframework/spock/issues/1270")
  def "Mock object boolean accessor via method" () {
    given:
    ExampleData mockData = GroovyMock(ExampleData)

    when: "query via method syntax"
    def result = mockData.isCurrent() ? "is enabled" : "is not enabled"

    then: "calls mock"
    1 * mockData.isCurrent() >> true

    and:
    result == "is enabled"
  }

  static class Person {
    void sing(String song) { throw new UnsupportedOperationException("sing") }
    String getName() { throw new UnsupportedOperationException("getName") }
    void setName(String name) { throw new UnsupportedOperationException("setName") }
  }

  static final class FinalPerson {
    void sing(String song) { throw new UnsupportedOperationException("sing") }
    String getName() { throw new UnsupportedOperationException("getName") }
    void setName(String name) { throw new UnsupportedOperationException("setName") }
  }

  static class FinalMethodsPerson {
    final void sing(String song) { throw new UnsupportedOperationException("sing") }
    final String getName() { throw new UnsupportedOperationException("getName") }
    final void setName(String name) { throw new UnsupportedOperationException("setName") }
  }
}




