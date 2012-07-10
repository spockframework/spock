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

import org.spockframework.mock.CannotCreateMockException

import spock.lang.Specification

class JavaSpies extends Specification {
  def "construct spied-on object using default constructor when no constructor args given (even if Objenesis is available on class path)"() {
    when:
    Spy(Constructable)

    then:
    thrown(CannotCreateMockException)
  }

  def "construct spied-on object using provided constructor args"() {
    def spy = Spy(Constructable, constructorArgs: ctorArgs)

    expect:
    spy.arg1 == arg1
    spy.arg2 == arg2
    spy.arg3 == arg3
    spy.arg4 == arg4

    where:
    ctorArgs | arg1 | arg2 | arg3 | arg4
    [1]      | 1    | 0    | 0    | null
    [2, 3]   | 0    | 2    | 3    | null
    ["hi"]   | 0    | 0    | 0    | "hi"
  }

  def "call real methods by default"() {
    def person = Spy(Person, constructorArgs: ["fred", 42])

    expect:
    person.name == "fred"
    person.age == 42
  }

  def "call real equals method by default"() {
    def fred1 = Spy(Person, constructorArgs: ["fred", 42])
    def fred2 = Spy(Person, constructorArgs: ["fred", 21])
    def barney = Spy(Person, constructorArgs: ["barney", 33])

    expect:
    fred1 == fred2
    fred1 != barney
  }

  def "call real hashCode method by default"() {
    def person = Spy(Person, constructorArgs: ["fred", 42])

    expect:
    person.hashCode() == "fred".hashCode()
  }

  def "call real toString method by default"() {
    def person = Spy(Person, constructorArgs: ["fred", 42])

    expect:
    person.toString() == "Hi, I'm fred"
  }

  def "can verify interactions with real methods"() {
    def person = Spy(Person, constructorArgs: ["fred", 42])

    when:
    def result = person.work()

    then:
    1 * person.work()
    1 * person.getTask()
    1 * person.getWorkHours()
    result == "singing, singing"
  }

  def "can be used as partial mocks"() {
    def person = Spy(Person, constructorArgs: ["fred", 42]) {
      getWorkHours() >>> [3, 2, 1]
    }

    expect:
    person.work() == "singing, singing, singing"
    person.work() == "singing, singing"
    person.work() == "singing"
  }

  static class Constructable {
    int arg1
    int arg2
    int arg3
    String arg4

    Constructable(int arg1) {
      this.arg1 = arg1
    }

    Constructable(int arg2, int arg3) {
      this.arg2 = arg2
      this.arg3 = arg3
    }

    Constructable(String arg4) {
      this.arg4 = arg4
    }
  }

  static class Person {
    String name
    int age
    List<String> children

    Person(String name, int age) {
      this.name = name
      this.age = age
    }

    def work() {
      ([task] * workHours).join(", ")
    }

    def getTask() {
      "singing"
    }

    def getWorkHours() {
      2
    }

    String toString() {
      "Hi, I'm $name"
    }

    boolean equals(Object other) {
      other instanceof Person && name == other.name
    }

    int hashCode() {
      name.hashCode()
    }
  }
}

