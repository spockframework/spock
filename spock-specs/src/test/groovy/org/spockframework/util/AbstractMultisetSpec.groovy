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

package org.spockframework.util

import spock.lang.Specification

abstract class AbstractMultisetSpec extends Specification {
  def multiset

  def "initially empty"() {
    expect:
    multiset.empty
    !multiset.iterator().hasNext()
    multiset.entrySet().empty
  }

  def "create multiset with different elements"() {
    multiset = new HashMultiset<String>(["foo", "bar"])

    expect:
    multiset.size() == 2
    multiset.contains("foo")
    multiset.count("foo") == 1
    multiset.contains("bar")
    multiset.count("bar") == 1
  }

  def "create multiset with same elements"() {
    multiset = new HashMultiset<String>(["foo", "foo"])

    expect:
    multiset.size() == 1
    multiset.contains("foo")
    multiset.count("foo") == 2
  }

  def "iterate over elements"() {
    multiset = new HashMultiset<String>(["foo", "bar", "foo"])
    def iterator = multiset.iterator()

    expect:
    iterator.hasNext()
    def elem1 = iterator.next()
    iterator.hasNext()
    def elem2 = iterator.next()
    [elem1, elem2] as Set == ["foo", "bar"] as Set
    !iterator.hasNext()
  }

  def "iterate over entry set"() {
    multiset = new HashMultiset<String>(["foo", "bar", "foo"])
    def entrySet = multiset.entrySet()

    expect:
    entrySet.size() == 2
    entrySet.find { it.key == "foo" && it.value == 2 }
    entrySet.find { it.key == "bar" && it.value == 1 }
  }

  def "add elements"() {
    expect:
    !multiset.contains("foo")

    when:
    def changed = multiset.add("foo")

    then:
    changed
    multiset.contains("foo")
    multiset.count("foo") == 1

    when:
    def changed2 = multiset.add("foo")

    then:
    changed2
    multiset.contains("foo")
    multiset.count("foo") == 2
  }

  def "remove elements"() {
    multiset = new HashMultiset<String>(["foo", "foo"])

    expect:
    multiset.contains("foo")
    multiset.count("foo") == 2

    when:
    def changed = multiset.remove("foo")

    then:
    changed
    multiset.contains("foo")
    multiset.count("foo") == 1

    when:
    def changed2 = multiset.remove("foo")

    then:
    changed2
    !multiset.contains("foo")
    multiset.count("foo") == 0

    expect:
    !multiset.remove("foo")
  }

  def "bulk add elements"() {
    when:
    def changed = multiset.addAll("foo", "bar", "foo")

    then:
    changed
    multiset.count("foo") == 2
    multiset.count("bar") == 1
  }

  def "bulk remove different elements"() {
    multiset = new HashMultiset<String>(["foo", "bar", "foo"])

    when:
    def changed = multiset.removeAll("foo", "bar")

    then:
    changed
    multiset.size() == 0
    !multiset.contains("foo")
    !multiset.contains("bar")
  }

  def "bulk remove same element"() {
    multiset = new HashMultiset<String>(["foo", "bar", "foo"])

    when:
    def changed = multiset.removeAll("foo", "foo")

    then:
    changed
    multiset.size() == 1
    multiset.count("foo") == 0
    multiset.count("bar") == 1
  }

  def "retain different elements"() {
    multiset = new HashMultiset<String>(["foo", "bar", "baz", "foo"])

    when:
    def changed = multiset.retainAll("foo", "baz")

    then:
    changed
    multiset.size() == 2
    multiset.contains("foo")
    multiset.count("foo") == 2
    !multiset.contains("bar")
    multiset.contains("baz")
    multiset.count("baz") == 1
  }

  def "retain same element"() {
    multiset = new HashMultiset<String>(["foo", "bar", "baz", "foo"])

    when:
    def changed = multiset.retainAll("bar", "bar")

    then:
    changed
    multiset.size() == 1
    !multiset.contains("foo")
    multiset.contains("bar")
    multiset.count("bar") == 1
    !multiset.contains("baz")
  }

  def "bulk contains"() {
    multiset = new HashMultiset<String>(["foo", "bar", "baz", "foo"])

    expect:
    multiset.containsAll("foo", "bar", "baz")
    !multiset.containsAll("foo", "bar", "bat")
  }

  def "clear"() {
    multiset = new HashMultiset<String>(["foo", "bar", "foo"])

    when:
    multiset.clear()

    then:
    multiset.size() == 0
    !multiset.contains("foo")
    !multiset.contains("bar")
  }

  def "convert to array"() {
    multiset = new HashMultiset<String>(["foo", "bar", "foo"])

    expect:
    multiset.toArray() == ["foo", "bar"] as Object[]

    def array = new Object[2]

    when:
    multiset.toArray(array)

    then:
    array == ["foo", "bar"] as Object[]
  }
}
