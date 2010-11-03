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

package org.spockframework.smoke.parameterization

import org.junit.internal.runners.model.MultipleFailureException

import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException
import org.spockframework.runtime.SpockExecutionException

import spock.lang.*

class DataTables extends EmbeddedSpecification {
  static staticField = 42

  @Shared
  def sharedField = 42

  def instanceField = 42

  def "basic usage"() {
    expect:
    Math.max(a, b) == c

    where:
    a | b | c
    5 | 7 | 7
    3 | 1 | 3
    9 | 9 | 9
  }

  def "table must have at least two columns"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a
1
    """

    then:
    MultipleFailureException e = thrown()
    e.failures*.class == [InvalidSpecCompileException] * 2
  }

  def "can use wildcards to effectively turn one-column table into two-column"() {
    expect:
    a == 1
    _ == _ // won't use this in practice

    where:
    a | _
    1 | _
  }

  def "table with just a header are not allowed"() {
    when:
    runner.runFeatureBody """
expect:
true

where:
a | b
    """

    then:
    thrown(SpockExecutionException)
  }

  def "header may only contain variable names"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a | 1 | b
1 | 1 | 1
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  def "header variable names must not clash with local variables"() {
    when:
    compiler.compileFeatureBody """
def local = 1

expect:
true

where:
a | local
1 | 1
    """

    then:
    thrown(InvalidSpecCompileException)
  }

    def "header variable names must not clash with each other"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a | a
1 | 1
    """

    then:
    thrown(InvalidSpecCompileException)
  }

  def "columns may be declared as parameters"(a, String b) {
    expect:
    a == 3
    b == "wow"

    where:
    a | b
    3 | "wow"
  }

  def "tables can be mixed with other parameterizations"() {
    expect:
    [a, b, c, d] == [1, 2, 3, 4]

    where:
    a << [1]
    b | c
    2 | 3
    d = c + 1
  }

  def "cells may contain arbitrary expressions"() {
    expect:
    a == "oo"
    b.age == 23
    c == 5

    where:
    a           | b                   | c
    "foo"[1..2] | new Person(age: 23) | Math.max(4, 5)
  }

  def "cells can reference shared and static fields"() {
    expect:
    a == 42
    b == 42

    where:
    a           | b
    staticField | sharedField
  }

  @Issue("http://issues.spockframework.org/detail?id=139")
  def "cells cannot reference instance fields (only @Shared and static fields)"() {
    when:
    compiler.compileSpecBody """
def instanceField

def foo() {
  expect:
  true

  where:
  a             | b
  instanceField | 1
}
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("@Shared")
  }

  def "cells cannot reference local variables (won't be found)"() {
    when:
    runner.runFeatureBody """
def local = 42

expect:
true

where:
a     | b
local | 1
    """

    then:
    thrown(MissingPropertyException)
  }

  def "cells cannot reference other cells"() {
    when:
    runner.runFeatureBody """
expect:
true

where:
a | b
1 | a
    """

    then:
    thrown(MissingPropertyException)
  }

  def "rows must have same number of elements as header"() {
    when:
    compiler.compileFeatureBody """
expect:
true

where:
a | b | c
1 | 2 | 3
4 | 5
    """

    then:
    thrown(InvalidSpecCompileException)
  }
}

private class Person {
  def age
}