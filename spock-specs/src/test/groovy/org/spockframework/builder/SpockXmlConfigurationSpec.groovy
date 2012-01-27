/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.builder

import spock.lang.Specification
import org.spockframework.runtime.SpockConfigurationBuilder
import org.spockframework.runtime.ModelSourceFactory

class SpockXmlConfigurationSpec extends Specification {
  def sourceFactory = new ModelSourceFactory()
  def builder = new SpockConfigurationBuilder()

  def "configure simple slots"() {
    builder.fromSource(sourceFactory.fromXml("""
<config>
  <person>
    <name>fred</name>
    <age>30</age>
  </person>
</config>
    """))
    def person = new Person()
    builder.build([person])

    expect:
    person.name == "fred"
    person.age == 30
  }

  def "configure object slot"() {
    builder.fromSource(sourceFactory.fromXml("""
<config>
  <person>
    <mother>
      <name>anna</name>
      <age>40</age>
    </mother>
  </person>
</config>
    """))

    def person = new Person()
    builder.build([person])

    expect:
    person.mother.name == "anna"
    person.mother.age == 40
  }
  
  def "configure collection slot"() {
    builder.fromSource(sourceFactory.fromXml("""
<config>
  <person>
    <friend>
      <name>tom</name>
      <age>20</age>
    </friend>
  </person>
</config>
    """))

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
