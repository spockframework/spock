package org.spockframework.builder

import org.spockframework.runtime.SpockConfigurationBuilder
import spock.lang.Specification

class SpockPropertiesConfigurationSpec extends Specification {
  def builder = new SpockConfigurationBuilder()

  def "configure simple slots"() {
    when:
    builder.fromProperties(["spock.person.name": "fred", "spock.person.age": 30])
    def spock = new SpockConfiguration()
    builder.build([spock])

    then:
    spock.person.name == "fred"
    spock.person.age == 30
    spock.person.balances == []
  }

  def "configure list slot"() {
    when:
    builder.fromProperties(["spock.person.balances": "3, 5, 8.9"])
    def spock = new SpockConfiguration()
    builder.build([spock])

    then:
    spock.person.name == null
    spock.person.age == 0
    spock.person.balances == [3.0, 5.0, 8.9]
  }

  def "test groovy"() {
    expect:
    "33".asType(BigDecimal)
  }
  
  private static class SpockConfiguration {
    Person person = new Person()
  }
  
  private static class Person {
    String name
    int age
    List<BigDecimal> balances = []
  }
}
