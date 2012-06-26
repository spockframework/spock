package org.spockframework.smoke.mock

import spock.lang.Specification

public class MockingOfInterfacesWithGroovyMocks extends Specification {
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

  interface Person {
    String getName()
    void setName(String name)
    void sing(String song)
  }
}




