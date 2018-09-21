/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.smoke.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.mock.TooFewInvocationsError

import org.hamcrest.CoreMatchers

class TooFewInvocations extends EmbeddedSpecification {
  def "shows unmatched invocations, ordered by similarity"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)
def list2 = Mock(List)
def map = Mock(Map)

when:
list.remove(2)
list2.add(2)
list.add(1)
map.size()
list.remove(2)

then:
1 * list.add(2)
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == '''
Too few invocations for:

1 * list.add(2)   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * list.add(1)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   1        |  2
            false
2 * list.remove(2)
methodName == "add"
|          |
remove     false
           6 differences (0% similarity)
           (remove)
           (add---)

1 * list2.add(2)
instance == target
|        |  |
|        |  Mock for type 'List' named 'list\'
|        false
|        1 difference (97% similarity)
|        Mock for type 'List' named 'list(2)\'
|        Mock for type 'List' named 'list(-)\'
Mock for type 'List' named 'list2\'

1 * map.size()
instance == target
|        |  |
|        |  Mock for type 'List' named 'list\'
|        false
|        8 differences (75% similarity)
|        Mock for type '(Map-)' named '(map-)\'
|        Mock for type '(List)' named '(list)\'
Mock for type 'Map' named 'map\'

methodName == "add"
|          |
size       false
           4 differences (0% similarity)
           (size)
           (add-)

<too few arguments>
    '''.trim()
  }

  def "makes it clear if no unmatched invocations exist"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
def x = 1

then:
1 * list.add(_)
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * list.add(_)   (0 invocations)

Unmatched invocations (ordered by similarity):

None
    """.trim()
  }

  def "shows all unsatisfied interactions"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

when:
list.add(2)
list.remove(2)

then:
1 * list.add(1)
1 * list.remove(1)
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * list.add(1)   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * list.add(2)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   2        |  1
            false
1 * list.remove(2)
methodName == "add"
|          |
remove     false
           6 differences (0% similarity)
           (remove)
           (add---)

One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   2        |  1
            false

Too few invocations for:

1 * list.remove(1)   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * list.remove(2)
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   2        |  1
            false
1 * list.add(2)
methodName == "remove"
|          |
add        false
           6 differences (0% similarity)
           (add---)
           (remove)

One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   2        |  1
            false
    """.trim()
  }

  def "does not show unverified invocations"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)
def list2 = Stub(List)

when:
list.add(1)
list2.add(2)

then:
1 * list.add(2)
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.contains("1 * list.add(1)")
    !e.message.contains("1 * list2.add(2)")
  }

  def "does not show invocations in outer scopes"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)

list.add(1)

when:
list.add(2)

then:
2 * list.add(_)
    """)

    then:
    TooFewInvocationsError e = thrown()
    !e.message.contains("list.add(1)")
  }

  def "can describe string argument mismatch"() {
    when:
    runner.addClassImport(Person)
    runner.runFeatureBody("""
def fred = Mock(Person)

when:
fred.wife("Fred", "Flintstone", 30, "Quarry")

then:
1 * fred.wife("Wilma", "Flintstone", 30, "Bedrock")
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * fred.wife("Wilma", "Flintstone", 30, "Bedrock")   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.wife('Fred', 'Flintstone', 30, 'Quarry')
One or more arguments(s) didn't match:
0: argument == expected
   |        |  |
   Fred     |  Wilma
            false
            5 differences (0% similarity)
            (Fred-)
            (Wilma)
1: <matches>
2: <matches>
3: argument == expected
   |        |  |
   Quarry   |  Bedrock
            false
            6 differences (14% similarity)
            (Qua)r(ry-)
            (Bed)r(ock)
    """.trim()
  }

  def "can describe code argument mismatch"() {
    when:
    runner.addClassImport(Person)
    runner.runFeatureBody("""
def fred = Mock(Person)

when:
fred.wife("Wilma", "Flintstone", 30, "Bedrock")

then:
1 * fred.wife("Wilma", "Flintstone", { it < 30}, "Bedrock")
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * fred.wife("Wilma", "Flintstone", { it < 30}, "Bedrock")   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.wife('Wilma', 'Flintstone', 30, 'Bedrock')
One or more arguments(s) didn't match:
0: <matches>
1: <matches>
2: <Code argument did not match>
3: <matches>
    """.trim()
  }

  def "can describe hamcrest matcher argument mismatch"() {
    when:
    runner.addClassImport(Person)
    runner.addClassImport(CoreMatchers)
    runner.runFeatureBody("""
def fred = Mock(Person)

when:
fred.wife("Wilma", "Flintstone", 30, "Bedrock")

then:
1 * fred.wife("Wilma", "Flintstone", CoreMatchers.equalTo(20), "Bedrock")
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * fred.wife("Wilma", "Flintstone", CoreMatchers.equalTo(20), "Bedrock")   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.wife('Wilma', 'Flintstone', 30, 'Bedrock')
One or more arguments(s) didn't match:
0: <matches>
1: <matches>
2: Expected: <20>
        but: was <30>
3: <matches>
    """.trim()
  }

  def "can describe type argument mismatch"() {
    when:
    runner.addClassImport(Person)
    runner.runFeatureBody("""
def fred = Mock(Person)

when:
fred.wife("Wilma", "Flintstone", 30, "Bedrock")

then:
1 * fred.wife("Wilma", "Flintstone", _ as String, _ as String)
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * fred.wife("Wilma", "Flintstone", _ as String, _ as String)   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.wife('Wilma', 'Flintstone', 30, 'Bedrock')
One or more arguments(s) didn't match:
0: <matches>
1: <matches>
2: argument instanceof java.lang.String
   |        |
   |        false
   30 (java.lang.Integer)
3: <matches>
    """.trim()
  }

  def "can describe type not enough args"() {
    when:
    runner.addClassImport(Person)
    runner.runFeatureBody("""
def fred = Mock(Person)

when:
fred.wife("Wilma", "Flintstone", 30, "Bedrock")

then:
1 * fred.wife("Wilma", "Flintstone",30, "Bedrock", _ as String)
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * fred.wife("Wilma", "Flintstone",30, "Bedrock", _ as String)   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.wife('Wilma', 'Flintstone', 30, 'Bedrock')
<too few arguments>
    """.trim()
  }

  def "can describe type too many args"() {
    when:
    runner.addClassImport(Person)
    runner.runFeatureBody("""
def fred = Mock(Person)

when:
fred.wife("Wilma", "Flintstone", 30, "Bedrock")

then:
1 * fred.wife("Wilma", "Flintstone",30)
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * fred.wife("Wilma", "Flintstone",30)   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.wife('Wilma', 'Flintstone', 30, 'Bedrock')
<too many arguments>
    """.trim()
  }

  def "can describe regex method name mismatch"() {
    when:
    runner.addClassImport(Person)
    runner.runFeatureBody("""
def fred = Mock(Person)

when:
fred.wife("Wilma", "Flintstone", 30, "Bedrock")

then:
1 * fred./kids?/("Wilma", "Flintstone", 30, "Bedrock")
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * fred./kids?/("Wilma", "Flintstone", 30, "Bedrock")   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.wife('Wilma', 'Flintstone', 30, 'Bedrock')
methodName ==~ /kids?/
|          |
wife       false
    """.trim()
  }

  def "can describe regex property name mismatch"() {
    when:
    runner.addClassImport(Person)
    runner.runFeatureBody("""
def fred = Mock(Person)

when:
def name = fred.age

then:
1 * fred./name?/
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * fred./name?/   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.getAge()
propertyName ==~ /name?/
|            |
age          false
    """.trim()
  }


  def "can describe property name mismatch"() {
    when:
    runner.addClassImport(Person)
    runner.runFeatureBody("""
def fred = Mock(Person)

when:
def name = fred.age

then:
1 * fred.name
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * fred.name   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.getAge()
propertyName == "name"
|            |
age          false
             2 differences (50% similarity)
             (-)a(g)e
             (n)a(m)e
    """.trim()
  }


  def "can describe target mismatch"() {
    when:
    runner.addClassImport(Person)
    runner.runFeatureBody("""
def fred = Mock(Person)
def barney = Mock(Person)

when:
def name = fred.name

then:
1 * barney.name
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * barney.name   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.getName()
instance == target
|        |  |
|        |  Mock for type 'Person' named 'barney'
|        false
|        4 differences (89% similarity)
|        Mock for type 'Person' named '(f-)r(-)e(d)'
|        Mock for type 'Person' named '(ba)r(n)e(y)'
Mock for type 'Person' named 'fred'
    """.trim()
  }

  def "can describe namedArgument on normal method mismatch"() {
    when:
    runner.addClassImport(Person)
    runner.runFeatureBody("""
def fred = Mock(Person)

when:
fred.wife("Wilma", "Flintstone", 30, "Bedrock")

then:
1 * fred.wife(firstName: "Wilma", surname: "Flintstone", age: _ as String, address: _ as String)
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * fred.wife(firstName: "Wilma", surname: "Flintstone", age: _ as String, address: _ as String)   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.wife('Wilma', 'Flintstone', 30, 'Bedrock')
<named arguments expected>
    """.trim()
  }

  def "can describe namedArgument on map method mismatch"() {
    when:
    runner.addClassImport(Person)
    runner.runFeatureBody("""
def fred = Mock(Person)

when:
fred.familiy(firstName: "Wilma", surname: "Flintstone", age: 30, address: "Quarry")

then:
1 * fred.familiy(firstName: "Wilma", surname: "Flintstone", age: _ as String, address: "Bedrock")
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() == """
Too few invocations for:

1 * fred.familiy(firstName: "Wilma", surname: "Flintstone", age: _ as String, address: "Bedrock")   (0 invocations)

Unmatched invocations (ordered by similarity):

1 * fred.familiy(['firstName':'Wilma', 'surname':'Flintstone', 'age':30, 'address':'Quarry'])
One or more arguments(s) didn't match:
[age]: argument instanceof java.lang.String
       |        |
       |        false
       30 (java.lang.Integer)
[address]: argument == expected
           |        |  |
           Quarry   |  Bedrock
                    false
                    6 differences (14% similarity)
                    (Qua)r(ry-)
                    (Bed)r(ock)
    """.trim()
  }


  def "can compute similarity between an interaction and a completely different invocation (without blowing up)"() {
    when:
    runner.addClassImport(Person)
    runner.runFeatureBody("""
def fred = Mock(Person)
def barney = Mock(Person)

when:
fred.age = 30

then:
1 * barney.setName { it.size() > 10 } // actual call passes an int, which has no size() method
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.contains("barney.setName")
  }


  def "renders with dump() if no better toString exists"() {
    when:
    runner.runFeatureBody("""
def list = Mock(List)
def person = new org.spockframework.smoke.mock.TooFewInvocations.Person(age: 18, name: 'Foo')

when:
list.add(person)

then:
1 * list.add('bar')
    """)

    then:
    TooFewInvocationsError e = thrown()
    e.message.trim() =~ /(?m)^\Q1 * list.add(<org.spockframework.smoke.mock.TooFewInvocations\E.Person@\w+ name=Foo age=18>\)$/
  }

  static class Person {
    String name
    int age

    String wife(String firstName, String surname, int age, String address){
      return ''
    }

    String familiy(Map args) {
      return ''
    }
  }
}
