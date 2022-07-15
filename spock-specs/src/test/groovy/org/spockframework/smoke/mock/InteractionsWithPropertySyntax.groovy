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

import org.spockframework.mock.TooManyInvocationsError

import spock.lang.*

class InteractionsWithPropertySyntax extends Specification {
  def lengthField = 20

  def "stubbing properties"() {
    def props = Mock(HasProperties)
    props.length >> 10

    expect:
    props.length == 10
  }

  def "stubbing properties in initializer closure with 'it'"() {
    def props = Mock(HasProperties) {
      it.length >> 10
    }

    expect:
    props.length == 10
  }

  def "stubbing properties in initializer closure without 'it'"() {
    def props = Mock(HasProperties) {
      length >> 10
    }

    expect:
    props.length == 10
  }

  def "stubbing properties in initializer closure without 'it' does not work if the property is also a parameter"(def length) {
    def props = Mock(HasProperties) {
      length >> 10
    }

    expect:
    props.length == 0

    where:
    length = 20
  }

  def "stubbing properties in initializer closure without 'it' does not work if the property is also a local variable"() {
    def length = 20
    def props = Mock(HasProperties) {
      length >> 10
    }

    expect:
    props.length == 0
  }

  def "stubbing properties in initializer closure without 'it' does not work if the property is also a field"() {
    def props = Mock(HasProperties) {
      lengthField >> 10
    }

    expect:
    props.lengthField == 0
  }

  def "stubbing properties in initializer closure without 'it' does not work if the property is also a spec property"() {
    def props = Mock(HasProperties) {
      lengthProperty >> 10
    }

    expect:
    props.lengthProperty == 0
  }

  def "mocking properties"() {
    def props = Mock(HasProperties)

    when:
    props.length
    props.length

    then:
    2 * props.length
  }

  def "stubbing/mocking boolean properties"() {
    def props = Mock(HasProperties)
    props.empty >> true
    props.full >> false

    expect:
    props.empty
    !props.full
  }

  @FailsWith(TooManyInvocationsError)
  def "setting a property cannot be mocked with property syntax"() {
    def props = Mock(HasProperties)

    when:
    props.length = 10

    then:
    1 * (props.length = 10)
    0 * _ // fails because previous line isn't considered an interaction
  }

  def "setting a property can be mocked with method syntax"() {
    def props = Mock(HasProperties)

    when:
    props.length = 10

    then:
    1 * props.setLength(10)
    0 * _
  }

  def "property names patterns are supported"() {
    def props = Mock(HasProperties)

    when:
    props.empty

    then:
    1 * props./length|empty/
    0 * _
  }

  int getLengthProperty() {
    lengthField
  }

  void setLengthProperty(int length) {
    lengthField = length
  }

  interface HasProperties {
    int getLength()
    void setLength(int length)

    int getLengthField()
    void setLengthField(int length)

    int getLengthProperty()
    void setLengthProperty(int length)

    boolean isEmpty()
    boolean getFull()
  }
}


