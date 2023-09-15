/*
 * Copyright 2008 the original author or authors.
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

package org.spockframework.smoke.condition

import org.opentest4j.AssertionFailedError
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionFailedWithExceptionError
import org.spockframework.runtime.ConditionNotSatisfiedError

import spock.lang.Issue
import spock.lang.FailsWith

import static java.lang.Math.max
import static java.lang.Math.min
import static java.lang.Integer.MAX_VALUE
import static java.lang.Thread.State.BLOCKED

/**
 * Checks that:
 * - condition transformation succeeds w/o compilation error (checked implicitly)
 * - condition evaluation succeeds w/o runtime error (checked explicitly)
 * - condition evaluation has the expected result (checked explicitly)
 *
 * @author Peter Niederwieser
 */

class ConditionEvaluation extends EmbeddedSpecification {
  def "multi-line condition"() {
    expect:
    2 *
        3 ==

        6
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "failing multi-line condition"() {
    expect:
    2 *
        3 ==

        7
  }

  def "MethodCallExpression"() {
    expect:
    [1, 2, 3].size() == 3
    [1, 2, 3].getClass().getMethod("size", null).parameterTypes.length == 0
    Integer.valueOf(String.valueOf(10)) == 10
  }

  def "MethodCallExpression with spread-dot operator"() {
    expect:
    ["1", "22"]*.size() == [1, 2]
  }

  def "MethodCallExpression with safe operator"() {
    def a = null

    expect:
    a?.foo() == null
  }

  @Issue("https://github.com/spockframework/spock/issues/375")
  @FailsWith(ConditionNotSatisfiedError)
  def "top-level MethodCallExpression with safe operator (method condition)"() {
    def a = null

    expect:
    a?.foo()
  }

  def "MethodCallExpression with named arguments"() {
    expect:
    new Person().eat(what: "steak", where: "tokyo") == [what: "steak", where: "tokyo"]
  }

  def "MethodCallExpression with named arguments passed as map"() {
    expect:
    new Person().eat([what: "steak", where: "tokyo"]) == [what: "steak", where: "tokyo"]
  }

  def "StaticMethodCallExpression"() {
    expect:
    max(1, 2) == 2
    max(min(1, 2), 3) == 3
  }

  def "ConstructorCallExpression"() {
    expect:
    new ArrayList().empty
    new String("abc") == "abc"
    new String(new String("abc")) == "abc"
  }

  def "ConstructorCallExpression with named arguments"() {
    expect:
    new Person(name: "fred", age: 25).name == "fred"
    new Person(name: "fred", age: 25).age == 25
  }

  def "ConstructorCallExpression with named arguments passed as map"() {
    expect:
    new Person([name: "fred", age: 25]).name == "fred"
    new Person([name: "fred", age: 25]).age == 25
  }

  def "TernaryExpression"() {
    expect:
    1 ? 1 : 0
    "abc".size() == 0 ? 0 : 1
  }

  def "ShortTernaryExpression"() {
    expect:
    1 ?: 0
    "".size() ?: 1
  }

  def "BinaryExpression"() {
    expect:
    1 == 1
    2 > 1 && 1 < 2
    1 * 1 / 1 + 1 - 1 ** 1 == 1
    1 == [[[[[1]]]]][0][0][0][0][0]
  }

  def "PrefixExpression"() {
    def x = 0

    expect:
    ++x == 1
    --x == 0
    x == 0
  }

  def "PostfixExpression"() {
    def x = 0

    expect:
    x++ == 0
    x-- == 1
    x == 0
  }

  def "BooleanExpression"() {
    expect:
    1
    "abc"
    [1, 2, 3]
    1 + 2 + 3
  }

  def "ClosureExpression"() {
    def x = 0

    when:
    def test = {it -> assert ++x == 1; {it2 -> assert ++x == 2 }(); {it3 -> assert ++x == 3 } }()

    then:
    x == 2

    when:
    test()

    then:
    x == 3
  }

  def "TupleExpression"() {
    def a, b

    expect:
    ((a, b) = [1, 2]) == [1, 2]
  }

  def "MapExpression"() {
    expect:
    ![:]
    [a: 1] + [b: 2] == [a: 1, b: 2]
  }

  def "ListExpression"() {
    expect:
    [1, 2, 3].size() == 3
    [] + [1] + [2, 3] == [1, 2, 3]
  }

  def "RangeExpression"() {
    expect:
    (1..3).contains(3)
    !((1..<3).contains(3))
  }

  def "PropertyExpression"() {
    expect:
    new Person(age: 3).age == 3
    (new Properties().next.next.next.x = 10) == 10
    Integer.MIN_VALUE < Integer.MAX_VALUE
  }

  def "AttributeExpression"() {
    def attrs = new Attributes()
    attrs.x = 1
    attrs.y = attrs

    expect:
    attrs.x == attrs.@x
    attrs.@y.@x == 1
  }

  def "MethodPointerExpression"() {
    def pointers = new MethodPointers()

    expect:
    pointers.&inc
    [1, 2, 3].collect(pointers.&inc) == [2, 3, 4]
  }

  def "ConstantExpression"() {
    expect:
    1
    1 == 1.0
    "abc".reverse() == "cba"
  }

  def "ClassExpression"() {
    expect:
    ConditionEvaluation == getClass()
    ConditionEvaluation.getClass() == Class.class
  }

  def "VariableExpression"() {
    def x = 1
    def y = 2

    expect:
    x < y
    x + y == 2 * y - x
    Math.max(x, y) == 2
  }

  def "RegexExpression"() {
    expect:
    (~"ab*a").matcher("abbba")
    !(~"ab*a").matcher("abcba")
  }

  def "GStringExpression"() {
    def x = 1
    def y = [1, 2, 3]

    expect:
    "$x and ${y.size()}" == "1 and 3"
  }

  def "ArrayExpression"() {
    expect:
    ([1, 2, 3] as int[]).size() == 3
  }

  private add(x, y) { x + y }

  def "SpreadExpression"() {
    expect:
    add(* [1, 2]) == 3
    [1, * [2, * [3, * [4]]]] == [1, 2, 3, 4]
  }

  private sub(args) { args.x - args.y }

  def "SpreadMapExpression"() {
    expect:
    sub(*: [y: 1, x: 2]) == 1
    [a: 1, b: 2, c: 3] == [c: 3, *: [b: 2, a: 1]]
  }

  def "NotExpression"() {
    expect:
    !false
    !(true && false)
  }

  def "UnaryMinusExpression"() {
    expect:
    -(-1) == 1
    -1 + -2 == -3
  }

  def "UnaryPlusExpression"() {
    expect:
    +(+2) == 2
    +1 + +2 == +3
  }

  def "BitwiseNegationExpression"() {
    expect:
    ~1 == -2
    ~~1 == 1
  }

  def "CastExpression"() {
    expect:
    (List) [1, 2, 3]
    ([1, 2, 3] as int[]).getClass().isArray()
  }

  def "ArgumentListExpression"() {
    expect:
    3.toString() == "3"
    Arrays.asList(1, 2, 3) == [1, 2, 3]
  }

  // as of Groovy 1.7.3, represented as FieldExpression
  @Issue("https://github.com/spockframework/spock/issues/228")
  def "statically imported field"() {
    expect:
    MAX_VALUE == 2147483647
  }

  // as of Groovy 1.7.3, represented as PropertyExpression
  def "statically imported enum value"() {
    expect:
    BLOCKED instanceof Thread.State
  }

  def "block condition (can not be parsed if flattened)"(){
    when:
    runner.runFeatureBody("""
        expect:
                def apply = {
                  Closure c -> c.call()
                }
                apply {
                  def x = 5
                  assert x
                  false
                }
        """)
    then:
    def failure = thrown(ConditionNotSatisfiedError)
    failure.getMessage() // should be rendered without errors

  }

  def "collection conditions work with various types"() {
    when:
    runner.runFeatureBody("""
        given:
        def a = [1,1,2,2,3,3] as ${aType}
        def b = [1,2,3] as ${bType}
        expect:
        a =~ b
        """)
    then:
    noExceptionThrown()

    where:
    [aType, bType] << ['int[]', 'Integer[]', 'List', 'Set', 'Queue', 'Deque'].with { [it, it] }.combinations()
  }

  def "strict collection conditions work with various types"() {
    when:
    runner.runFeatureBody("""
        given:
        def a = [1,1,2,2,3,3] as ${aType}
        def b = [1,2,3,3,2,1] as ${bType}
        expect:
        a ==~ b
        """)
    then:
    noExceptionThrown()

    where:
    [aType, bType] << ['int[]', 'Integer[]', 'List', 'Queue', 'Deque'].with { [it, it] }.combinations()
  }

  @FailsWith(ConditionFailedWithExceptionError)
  def "regular implicit condition"() {
    expect:
    customContains("foo", "bar")
  }

  @FailsWith(AssertionFailedError)
  def "opt-out of implicit-condition handling does not wrap exceptions"() {
    expect:
    !!customContains("foo", "bar")
  }

  def "opt-out of implicit-condition handling won't fail for falsy values"() {
    expect:
    !!aList.each { assert it > 0 }

    where:
    aList << [[1], []]
  }

  def "opt-out of implicit-condition handling won't fail static method conditions returning falsy value"() {
    expect:
    !!min(0, 0)
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "opt-out only works if it is the outermost expression"() {
    expect:
    !!false && false
  }


  def "opt-out only works for the outermost expression"() {
    expect:
    !!(true && false)
    !!true
    !!false
  }

  /*
  def "MapEntryExpression"() {
      // tested as part of testMapExpression
  }

  def "DeclarationExpression"() {
      // cannot occur in condition
  }

  def "RegexExpression"() {
      // unused AST node
  }

  def "ClosureListExpression"() {
      // cannot occur in condition
  }

  def "BytecodeExpression"() {
      // cannot occur in condition
  }
  */

  void customContains(String a, String b) {
    if(!a.contains(b)) {
      throw new AssertionFailedError("'$a' does not contain '$b'", a, b)
    }
  }

  static class Properties {
    def getNext() { this }

    def x
  }

  static class Attributes {
    def x
    def y
  }

  static class MethodPointers {
    def inc(x) { x + 1 }
  }

  static class Person {
    def name
    def age
    def height

    def eat(args) { args }
  }
}
