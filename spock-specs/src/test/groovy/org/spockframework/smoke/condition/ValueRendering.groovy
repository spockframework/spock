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

import spock.lang.Issue

import static java.lang.Thread.State.NEW

/**
 * Describes rendering of individual values.
 *
 * @author Peter Niederwieser
 */

class ValueRendering extends ConditionRenderingSpec {
  def "null value"() {
    expect:
    isRendered """
x
|
null
    """, {
      def x = null
      assert x
    }
  }

  def "char value"() {
    expect:
    isRendered """
x == null
| |
c false
    """, {
      def x = "c" as char
      assert x == null
    }
  }

  def "string value"() {
    expect:
    isRendered """
x == null
| |
| false
foo
    """, {
      def x = "foo"
      assert x == null
    }
  }

  @Issue("https://github.com/spockframework/spock/issues/140")
  def "empty string value"() {
    isRendered """
x == null
| |
| false
""
    """, {
      def x = ""
      assert x == null
    }
  }

  def "multi-line string value"() {
    expect:
    isRendered """
null == x
     |  |
     |  one
     |  two
     |  three
     |  four
     false
    """, {
      def x = "one\ntwo\rthree\r\nfour"
      assert null == x
    }
  }

  def "list value"() {
    expect:
    isRendered """
x == null
| |
| false
[1, 2, 3]
    """, {
      def x = [1, 2, 3]
      assert x == null
    }
  }

  def "map value"() {
    expect:
    isRendered """
x == null
| |
| false
[a:1, b:2]
    """, {
      def x = [a: 1, b: 2]
      assert x == null
    }
  }

  def "single-line toString"() {
    expect:
    isRendered """
x == null
| |
| false
single line
    """, {
      def x = new SingleLineToString()
      assert x == null
    }
  }

  def "multi-line toString"() {
    expect:
    isRendered """
x == null
| |
| false
mul
tiple
   lines
    """, {
      def x = new MultiLineToString()
      assert x == null
    }
  }

  def "null toString"() {
    expect:
    def x = new NullToString()

    isRendered """
x == null
| |
| false
${x.objectToString().replace '$NullToString', '.NullToString'}
    """, {
      assert x == null
    }
  }

  def "empty toString"() {
    def x = new EmptyToString()

    expect:
    isRendered """
x == null
| |
| false
${x.objectToString().replace '$EmptyToString', '.EmptyToString'}
    """, {
      assert x == null
    }
  }

  def "exception-throwing toString"() {
    def x = new ThrowingToString()

    expect:
    isRendered """
x == null
| |
| false
${x.objectToString().replace '$ThrowingToString', '.ThrowingToString'} (renderer threw UnsupportedOperationException)
    """, {
      assert x == null
    }
  }

  def "enum literal"() {
    expect:
    isRendered '''
Thread.State.NEW == null
       |         |
       |         false
       class java.lang.Thread$State
    ''', {
      assert Thread.State.NEW == null
    }
  }

  def "statically imported enum literal"() {
    expect:
    isRendered """
NEW == null
    |
    false
    """, {
      assert NEW == null
    }
  }

  def "enum literal with toString"() {
    expect:
    isRendered '''
EnumWithToString.VALUE == null
|                |     |
|                |     false
|                I'm a value
class org.spockframework.smoke.condition.ValueRendering$EnumWithToString
    ''', {
      assert EnumWithToString.VALUE == null
    }
  }

  def "variable with enum value"() {
    expect:
    isRendered """
x == null
| |
| false
NEW
    """, {
      def x = NEW
      assert x == null
    }
  }

  def "variable with default to string is dump()ed"() {
    def x = new DefaultToString()
    expect:
    isRendered """
x == null
| |
| false
${x.dump()}
    """, {
      assert x == null
    }
  }

  static class SingleLineToString {
    String toString() {
      "single line"
    }
  }

  static class MultiLineToString {
    String toString() {
      "mul\ntiple\n   lines"
    }
  }

  static class NullToString {
    String objectToString() {
      super.toString()
    }

    String toString() { null }
  }

  static class EmptyToString {
    String objectToString() {
      super.toString()
    }

    String toString() { "" }
  }

  static class ThrowingToString {
    String objectToString() {
      super.toString()
    }

    String toString() {
      throw new UnsupportedOperationException()
    }
  }


  static class DefaultToString {
    int a = 4
  }

  enum EnumWithToString {
    VALUE;

    String toString() { "I'm a value" }
  }
}

