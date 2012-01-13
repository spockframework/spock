package org.spockframework.builder

import spock.lang.Specification
import org.spockframework.runtime.SpockConfigurationBuilder

class SpockXmlConfigurationSpec extends Specification {
  def builder = new SpockConfigurationBuilder()

  def "configure simple slots"() {
    builder.fromXml """
<config>
  <person>
    <name>fred</name>
    <age>30</age>
  </person>
</config>
    """ 
    def person = new Person()
    builder.build([person])

    expect:
    person.name == "fred"
    person.age == 30
  }

  def "configure object slot"() {
    builder.fromXml """
<config>
  <person>
    <mother>
      <name>anna</name>
      <age>40</age>
    </mother>
  </person>
</config>
    """

    def person = new Person()
    builder.build([person])

    expect:
    person.mother.name == "anna"
    person.mother.age == 40
  }
  
  def "configure collection slot"() {
    builder.fromXml """
<config>
  <person>
    <friend>
      <name>tom</name>
      <age>20</age>
    </friend>
  </person>
</config>
    """

    def person = new Person()
    builder.build([person])

    expect:
    person.friends.size() == 1
    def friend = person.friends[0]
    friend.name == "tom"
    friend.age == 20
  }
  
  private static class Person {
    String name
    int age
    Person father
    Person mother
    List<Person> friends
  }
}
