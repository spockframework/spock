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

package org.spockframework.smoke.mock

import spock.lang.*

class WildcardUsages extends Specification {
  def "match any argument"() {
    def list = Mock(List)

    when:
    list.subList(1, 1)
    list.subList(2, 1)
    list.subList(3, 2)

    then:
    2 * list.subList(_, 1)
  }

  def "match any argument that has a certain type"() {
    def list = Mock(List)

    when:
    list.add(new HashMap())
    list.add(new TreeMap())
    list.add("string")

    then:
    2 * list.add(_ as Map)
  }

  def "match any target"() {
    def list1 = Mock(List)
    def list2 = Mock(List)

    when:
    list1.get(1)
    list2.add("abc")

    then:
    1 * _.add("abc")
    1 * _.get(1)
  }

  def "match any method name"() {
    def list1 = Mock(List)
    def list2 = Mock(List)

    when:
    list1.get(1)
    list1.add(1)
    list1.get(2)
    list2.add(2)

    then:
    2 * list1._(1)
  }

  def "match any method name and argument list"() {
    def list = Mock(List)

    when:
    list.clear()
    list.get(1)
    list.subList(3, 5)

    then:
    3 * list._
  }

  def "match any invocation on a mock object"() {
    when:
    Mock(XYZ).get(1)
    Mock(XYZ).get(2)
    Mock(XYZ).get(3)

    then:
    3 * _._
  }

  def "match any invocation on a mock object (short syntax)"() {
    when:
    Mock(XYZ).get(1)
    Mock(XYZ).get(2)
    Mock(XYZ).get(3)

    then:
    3 * _
  }

  @Issue("https://github.com/spockframework/spock/issues/460")
  def "wildcard method name doesn't match Object's equals(), hashCode(), toString(), and finalize() methods"() {
    // needs to be a class, otherwise finalize() call wouldn't get through in any case
    // (probably a difference between Java and CGLIB proxies)
    def list = Mock(ArrayList)

    when:
    list.equals(new Object())
    list.hashCode()
    list.toString()
    list.finalize()

    then:
    0 * list._()
    0 * list._
    0 * _
  }

  def "wildcard method name matches overloaded equals(), hashCode(), toString(), and finalize() methods"() {
    def overloaded = Mock(Overloaded)

    when:
    overloaded.equals("me")
    overloaded.hashCode([])
    overloaded.toString(true)
    overloaded.finalize(true)

    then:
    4 * overloaded._
  }

  def "usage in interaction doesn't interfere with usage in where-block"() {
    def list = Mock(type)

    when:
    list.add(1)

    then:
    1 * _._(_)

    where:
    [_, type] << [[123, List]]
  }
}

interface XYZ {
 void get(int i)
}

interface Overloaded {
  boolean equals(String str)
  int hashCode(List list)
  String toString(boolean verbose)
  void finalize(boolean now)
}
