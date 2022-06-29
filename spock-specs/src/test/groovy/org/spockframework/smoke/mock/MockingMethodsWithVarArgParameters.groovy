
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
import org.spockframework.mock.TooFewInvocationsError

@Issue("https://github.com/spockframework/spock/issues/177")
class MockingMethodsWithVarArgParameters extends Specification {
  def "interactions of methods callable in vararg-style can be written in vararg-style"() {
    def mock = Mock(clazz)

    when:
    mock.foo(1, "one", "two", "three") // irrelevant which style is used here (i.e. by subject)

    then:
    1 * mock.foo(1, "one", "two", "three")

    where:
    clazz << [GroovyVarArgParameter, GroovyArrayParameter]
  }

  def "interactions of methods callable in vararg-style can be written in array-style"() {
    def mock = Mock(clazz)

    when:
    mock.foo(1, "one", "two", "three") // irrelevant which style is used here (i.e. by subject)

    then:
    1 * mock.foo(1, ["one", "two", "three"] as String[])

    where:
    clazz << [GroovyVarArgParameter, GroovyArrayParameter]
  }

  def "interactions of methods callable in vararg-style can be written in list-style"() {
    def mock = Mock(clazz)

    when:
    mock.foo(1, "one", "two", "three") // irrelevant which style is used here (i.e. by subject)

    then:
    1 * mock.foo(1, ["one", "two", "three"]) // works because Groovy equality covers comparison between array and list

    where:
    clazz << [GroovyVarArgParameter, GroovyArrayParameter]
  }

  @FailsWith(TooFewInvocationsError)
  def "interactions of methods not callable in vararg-style cannot be written in vararg-style"() {
    def mock = Mock(NoVarArgParameter)

    when:
    mock.foo(1, ["one", "two"] as String[])

    then:
    1 * mock.foo(1, "one", "two")
  }

  def "one more argument than constraints - passing example"() {
    def mock = Mock(GroovyVarArgParameter)

    when:
    mock.foo(1, [] as String[])

    then:
    1 * mock.foo(1)
  }

  @FailsWith(TooFewInvocationsError)
  def "one more argument than constraints - failing example"() {
    def mock = Mock(GroovyVarArgParameter)

    when:
    mock.foo(1, ["one"] as String[])

    then:
    1 * mock.foo(1)
  }

  def "equal number of arguments and constraints - passing example"() {
    def mock = Mock(GroovyVarArgParameter)

    when:
    mock.foo(1, ["one"] as String[])

    then:
    1 * mock.foo(1, "one")
  }

  @FailsWith(TooFewInvocationsError)
  def "equal number of arguments and constraints - failing example"() {
    def mock = Mock(GroovyVarArgParameter)

    when:
    mock.foo(1, ["one", "two"] as String[])

    then:
    1 * mock.foo(1, "one")
  }

  def "fewer arguments than constraints - passing example"() {
    def mock = Mock(GroovyVarArgParameter)

    when:
    mock.foo(1, ["one", "two"] as String[])

    then:
    1 * mock.foo(1, "one", "two")
  }

  @FailsWith(TooFewInvocationsError)
  def "fewer arguments than constraints - failing example"() {
    def mock = Mock(GroovyVarArgParameter)

    when:
    mock.foo(1, ["one"] as String[])

    then:
    1 * mock.foo(1, "one", "two")
  }

  @FailsWith(TooFewInvocationsError)
  def "vararg constraint satisfied but other constraint fails"() {
    def mock = Mock(GroovyVarArgParameter)

    when:
    mock.foo(1, "one", "two")

    then:
    1 * mock.foo(2, "one", "two")
  }

  def "vararg value can be null"() {
    def mock = Mock(GroovyVarArgParameter)

    when:
    mock.foo(1, null)

    then:
    1 * mock.foo(1, null)
  }

  @Issue("https://github.com/spockframework/spock/issues/367")
  def "handles case where vararg value is null and interaction does not match"() {
    def mock = Mock(GroovyVarArgParameter)

    when:
    mock.foo(1, null)

    then:
    0 * mock.foo(2, null)
  }

  def "vararg value that is omitted in invocation can be omitted in interaction"() {
    def mock = Mock(GroovyVarArgParameter)

    when:
    mock.foo(1)

    then:
    1 * mock.foo(1)
  }

  def "vararg value that is omitted in invocation can be specified as empty array in interaction"() {
    def mock = Mock(GroovyVarArgParameter)

    when:
    mock.foo(1)

    then:
    1 * mock.foo(1, new Object[0])
  }

  @Issue("https://github.com/spockframework/spock/issues/416")
  def "vararg handling also works when element type is a primitive type"() {
    def mock = Mock(clazz)

    when:
    mock.foo("one", 1, 2, 3)
    mock.foo("one", [1, 2, 3] as int[])

    then:
    2 * mock.foo("one", 1, 2, 3)

    when:
    mock.foo("one", 1, 2, 3)
    mock.foo("one", [1, 2, 3] as int[])

    then:
    2 * mock.foo("one", [1, 2, 3] as int[])

    where:
    clazz << [GroovyPrimitiveVarArgParameter, GroovyPrimitiveArrayParameter]
  }

  interface GroovyVarArgParameter {
    def foo(int i, String... strings)
  }

  interface GroovyArrayParameter {
    def foo(int i, String[] strings)
  }

  interface GroovyPrimitiveVarArgParameter {
    def foo(String str, int... strings)
  }

  interface GroovyPrimitiveArrayParameter {
    def foo(String str, int[] bytes)
  }

  interface NoVarArgParameter {
    def foo(int i, strings)
  }
}


