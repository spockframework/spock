/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.smoke.condition

import spock.lang.*

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.InvalidSpecException
import org.spockframework.runtime.ConditionNotSatisfiedError

import static org.hamcrest.CoreMatchers.*
import static spock.util.matcher.HamcrestSupport.that
import static spock.util.matcher.HamcrestSupport.expect

class MatcherConditions extends EmbeddedSpecification {
  def "work in expect-blocks"() {
    def x = 42

    expect:
    x equalTo(42)
  }

  def "work in then-blocks"() {
    when:
    def x = 42

    then:
    x equalTo(42)
  }

  def "have an alternative 'that' syntax"() {
    def x = 42

    expect:
    that x, equalTo(42)
  }

  def "have an alternative 'expect' syntax"() {
    when:
    def x = 42

    then:
    expect x, equalTo(42)
  }

  def "can be explicit conditions (but only with 'that' or 'expect' syntax)"() {
    setup:
    def x = 42
    assert that(x, equalTo(42))
  }

  def "can provide a custom message"() {
    def x = 42

    expect:
    assert that(x, equalTo(42)), "a custom message"
  }

  def "work with custom matcher implementations"() {
    def x = "otto"
    
    expect:
    x palindrome()
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "fail when matcher doesn't match (1)"() {
    def x = 21

    expect:
    x equalTo(42)
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "fail when matcher doesn't match (2)"() {
    when:
    def x = 21

    then:
    x equalTo(42)
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "fail when matcher doesn't match (3)"() {
    def x = 21

    expect:
    that x, equalTo(42)
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "fail when matcher doesn't match (4)"() {
    setup:
    def x = 21
    assert that(x, equalTo(42))
  }

  @FailsWith(MissingMethodException)
  def "can only be used where a condition is expected (1)"() {
    setup:
    def x = 42
    x equalTo(42)
  }

  @FailsWith(InvalidSpecException)
  def "can only be used where a condition is expected (2)"() {
    setup:
    def x = 42
    that x, equalTo(42)
  }

  def "can have a numeric literal as actual"() {
    def x = 42

    expect:
    42 equalTo(x)
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "cannot have a string literal as actual (because matcher expression gets parsed as a method call)"() {
    def x = "fred"

    expect:
    "fred" equalTo(x)
  }

  def "can have a string literal as actual if 'that' or 'expect' syntax is used"() {
    def x = "fred"

    expect:
    that "fred", equalTo(x)    
  }

  class Foo {
    String method() { "method" }
    String property = "property"
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "cannot have a method expression as actual in Groovy 1.8 (because matcher expression gets parsed as a chained method call)"() {
    def foo = new Foo()

    expect:
    foo.method() equalTo("method")
  }

  def "can have a method expression as actual if 'that' or 'expect' syntax is used"() {
    def foo = new Foo()

    expect:
    that foo.method(), equalTo("method")
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "cannot have a property expression as actual (because matcher expression gets parsed as a method call)"() {
    def foo = new Foo()

    expect:
    foo.property equalTo("property")
  }

  def "can have a property expression as actual if 'that' or 'expect' syntax is used"() {
    def foo = new Foo()

    expect:
    that foo.property, equalTo("property")
  }

  def "hamcrest matchers are also supported as constraints in interactions"() {
    def list = Mock(List)

    when:
    list.add(42)

    then:
    0 * list.add(equalTo(21))
    1 * list.add(equalTo(42))
  }

  private static palindrome() {
    new IsPalindrome()
  }

  static class IsPalindrome extends BaseMatcher<String> {
    boolean matches(Object value) {
      value instanceof String && value.reverse() == value
    }

    void describeTo(Description description) {
      description.appendText("a palindrome")
    }
  }
}
