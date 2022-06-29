/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.builder

import spock.lang.Specification

class PojoBuilderSpec extends Specification {
  Person person = new Person()
  PojoBuilder builder = new PojoBuilder()

  def "configure top-level property"() {
    when:
    builder.build(person) {
      name "fred"
    }

    then:
    person.name == "fred"
  }

  def "configure nested property"() {
    when:
    builder.build(person) {
      address {
        street "columbo road"
      }
    }

    then:
    person.address.street == "columbo road"
  }

  def "configure collection property"() {
    when:
    builder.build(person) {
      gadget "iphone"
      gadget "blackberry"
    }

    then:
    person.gadgets == ["iphone", "blackberry"]
  }


  def "configure collection property that has concrete declared type"() {
    when:
    def house = new House()
    builder.build(house) {
      room "living room"
      room "dining room"
    }

    then:
    house.rooms instanceof TreeSet
    house.rooms == ["living room", "dining room"] as Set
  }

  def "configure collection property and choose collection type"() {
    when:
    def computer = new Computer()
    builder.build(computer) {
      addresses new LinkedList()
      address { city "munich" }
      address { city "vienna" }
    }

    then:
    computer.addresses instanceof LinkedList
    computer.addresses.size() == 2
  }

  def "adding elements to collection"() {
    when:
    def elements = new Elements()
    builder.build(elements) {
      clock "tic tac"
      address "mercury road"
      rowdy "fred"
    }

    then:
    elements.clocks == ["tic tac"]
    elements.addresses == ["mercury road"]
    elements.rowdies == ["fred"]
  }

  def "configure primitives"() {
    when:
    def pr = new Primitives()
    builder.build(pr) {
      aByte 0 as byte
      aShort 300 as short
      anInt 70000
      aLong 3000000000l
      aFloat 0.1 as float
      aDouble 0.2
      aChar "a" as char
      aBoolean true
    }

    then:
    pr.aByte == 0
    pr.aShort == 300
    pr.anInt == 70000
    pr.aLong == 3000000000
    pr.aFloat == 0.1 as float
    pr.aDouble == 0.2
    pr.aChar == "a" as char
    pr.aBoolean == true
  }

  def "access local variable"() {
    def contextVar = "flamingo rd."

    when:
    builder.build(person) {
      address {
        street contextVar
      }
    }

    then:
    person.address.street == contextVar
  }

  def aField = "pancorn av."

  def "access field"() {
    when:
    builder.build(person) {
      address {
        street aField
      }
    }

    then:
    person.address.street == aField
  }
}

class Person {
  String name
  Address address
  List<String> gadgets
}

class Address {
  String street
  String city
}

class House {
  TreeSet<String> rooms
}

class Computer {
  List<Address> addresses
}

class Elements {
  List<String> clocks
  List<String> addresses
  List<String> rowdies
}

class Primitives {
  byte aByte
  short aShort
  int anInt
  long aLong
  float aFloat
  double aDouble
  char aChar
  boolean aBoolean
}
