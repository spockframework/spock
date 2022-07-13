/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.util

import spock.lang.Specification

class JsonWriterSpec extends Specification {
  def stringWriter = new StringWriter()
  def jsonWriter = new JsonWriter(stringWriter)

  def "can encode basic values"() {
    when:
    jsonWriter.write(input)

    then:
    stringWriter.toString() == output

    where:
    input                   | output
    null                    | "null"
    42                      | "42"
    -42                     | "-42"
    3.45                    | "3.45"
    [1, 2, 3] as int[]      | "[1,2,3]"
    [1, 2, 3]               | "[1,2,3]"
    [name: "Fred", age: 32] | '{"name":"Fred","age":32}'
    new Date(1359975046334) | '"2013-02-04T10:50:46+0000"'
    "foobar"                | '"foobar"'
    new ToString()          | '"hello, world!"'
  }

  def "can handle nested values"() {
    when:
    jsonWriter.write(["foobar", [name: "Fred", age: [new Date(1359975046334), null]], 3.45])

    then:
    stringWriter.toString() == '["foobar",{"name":"Fred","age":["2013-02-04T10:50:46+0000",null]},3.45]'
  }

  def "escapes special String characters"() {
    when:
    jsonWriter.write(input)

    then:
    stringWriter.toString() == output

    where:
    input                    | output
    "foo\b\n\t\f\rbar\u1234" | '"foo\\b\\n\\t\\f\\rbar\\u1234"'
    "\"\\/"                  | '"\\"\\\\\\/"'
  }

  def "does not escape single quotes"() {
    when:
    jsonWriter.write(input)

    then:
    stringWriter.toString() == output

    where:
    input                    | output
    "foo'bar'baz"            | "\"foo'bar'baz\""
  }

  def "complains about NaN and Infinity"() {
    when:
    jsonWriter.write(input)

    then:
    thrown(IllegalArgumentException)

    where:
    input << [Float.NaN, Double.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY]
  }

  def "complains about null toString()"() {
    when:
    jsonWriter.write(new NullToString())

    then:
    thrown(IllegalArgumentException)
  }

  def "can pretty print output"() {
    def prettyWriter = new JsonWriter(stringWriter)
    prettyWriter.prettyPrint = true
    prettyWriter.indent = "  "

    when:
    prettyWriter.write(["foobar", [name: "Fred", age: [new Date(1359975046334), null]], 3.45])

    then:
    stringWriter.toString() ==
        """
[
  "foobar",
  {
    "name": "Fred",
    "age": [
      "2013-02-04T10:50:46+0000",
      null
    ]
  },
  3.45
]
""".trim()
  }

  private static class ToString {
    String toString() {
      "hello, world!"
    }
  }

  private static class NullToString {
    String toString() {
      null
    }
  }
}
