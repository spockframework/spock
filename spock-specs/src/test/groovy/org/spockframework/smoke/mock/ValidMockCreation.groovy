/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.mock

import spock.lang.Specification

class ValidMockCreation extends Specification {
  List list1 = Mock()

  def "field typed w/ mock untyped"() {
    expect:
    list1 instanceof List
  }

  List list1a

  def "field typed w/ mock untyped, separate assignment"() {
    list1a = Mock()

    expect:
    list1a instanceof List
  }

  List list2 = Mock(ArrayList)

  def "field typed w/ mock typed"() {
    expect:
    list2 instanceof ArrayList
  }

  List list2a

  def "field typed w/ mock typed, separate assignment"() {
    list2a = Mock(ArrayList)

    expect:
    list2a instanceof ArrayList
  }

  def list3 = Mock(List)

  def "field untyped w/ mock typed"() {
    expect:
    list3 instanceof List
  }

  def "local typed w/ mock untyped"() {
    List list = Mock()

    expect:
    list instanceof List
  }

  def "local typed with w/ mock untyped, separate assignment"() {
    List list
    list = Mock()

    expect:
    list instanceof List
  }

  def "local typed w/ mock typed"() {
    List list = Mock(ArrayList)

    expect:
    list instanceof ArrayList
  }

  def "local typed w/ mock typed, separate assignment"() {
    List list
    list = Mock(ArrayList)

    expect:
    list instanceof ArrayList
  }

  def "local untyped w/ mock typed"() {
    def list = Mock(List)

    expect:
    list instanceof List
  }

  def "expr typed"() {
    expect:
    Mock(List) instanceof List
  }

  def "creation in nested expr"() {
    def list = null
    if (1) list = id(id(Mock(List)))

    expect:
    list instanceof List
  }

  def "creation in closure"() {
    // a closure preceded by a label is parsed as block by Groovy,
    // so we use "assert" instead of "expect:" here
    assert { it -> { it2 -> Mock(List) }() }() instanceof List
  }

  def "creation in interaction result"() {
    List list = Mock()
    list.get(_) >> Mock(Map)

    expect:
    list.get(42) instanceof Map
  }

  private id(arg) { arg }
}
