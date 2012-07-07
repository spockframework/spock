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

import spock.lang.Specification

public class MockingOfGroovyClassesWithGroovyMocks extends Specification {
  def person = GroovyMock(Person)

  def "physical method"() {
    when:
    person.sing("song")

    then:
    1 * person.sing("song")
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

  static class Person {
    void sing(String song) { throw new UnsupportedOperationException("sing") }
    String getName() { throw new UnsupportedOperationException("getName") }
    void setName(String name) { throw new UnsupportedOperationException("setName") }
  }
}



