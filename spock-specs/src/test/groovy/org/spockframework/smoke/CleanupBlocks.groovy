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

package org.spockframework.smoke

import org.spockframework.EmbeddedSpecification
import spock.lang.*

import org.codehaus.groovy.runtime.typehandling.GroovyCastException

import static org.spockframework.runtime.model.parallel.ExecutionMode.SAME_THREAD

@Execution(SAME_THREAD)
class CleanupBlocks extends EmbeddedSpecification {
  static List log

  def setup() {
    log = []
  }

  def "basic usage"() {
    def x
    setup: x = 1
    expect: x == 1
    cleanup: x = 0
  }

  def "may access all previously defined local vars"() {
    def a

    setup:
    def b
    when:
    String c
    then:
    List d

    cleanup:
    a = 0
    b = 0
    c = ""
    d = null
  }

  @FailsWith(IllegalArgumentException)
  def "is executed if no exception is thrown"() {
    def a = 1

    cleanup:
    a = 0
    if (a == 0) throw new IllegalArgumentException()
  }

  def "is executed if exception is thrown"() {
    when:
    runner.runSpecBody """
def getLog() { org.spockframework.smoke.CleanupBlocks.log }

def feature() {
  log << "feature"
  throw new IllegalArgumentException()

  cleanup:
  log << "cleanup"
}
"""

    then:
    thrown(IllegalArgumentException)
    log == ["feature", "cleanup"]
  }

  @Issue('https://github.com/spockframework/spock/issues/142')
  def "feature exception and cleanup exception are both reported"() {
    // turn off throwing the first failure so all failures can be inspected
    runner.throwFailure = false

    when:
    def result = runner.runSpecBody """
def getLog() { org.spockframework.smoke.CleanupBlocks.log }

def feature() {
  log << "feature"
  throw new IllegalArgumentException("feature")

  cleanup:
  log << "cleanup"
  throw new IllegalArgumentException("cleanup")
}
"""

    then:
    def failure = result.failures[0]
    failure.exception.message == "feature"
    failure.exception.suppressed[0].message == "cleanup"
    log == ["feature", "cleanup"]
  }

  @FailsWith(IllegalArgumentException)
  def "if exception is thrown, code between occurrence of exception and cleanup-block is not executed"() {
    def a = 1
    throw new IllegalArgumentException()
    a = 2

    cleanup:
    assert a == 1
  }

  @Issue("https://github.com/spockframework/spock/issues/388")
  def "variable with primitive type can be declared in presence of cleanup-block"() {
    int x = 1

    expect:
    x == 1

    cleanup:
    []
  }

  def "variable with primitive type can be read in cleanup-block"() {
    int x = 1

    cleanup:
    assert x == 1
  }

  @FailsWith(GroovyCastException)
  def "declared type of variable is kept"() {
    int x = "abc"

    cleanup:
    []
  }

  @Issue("https://github.com/spockframework/spock/issues/493")
  def "multi-declaration with primitive type in presence of cleanup-block"() {
    when:
    def (String foo, int bar) = ["foo", 42]

    then:
    foo == "foo"
    bar == 42

    cleanup:
    assert foo == "foo"
    assert bar == 42
  }

  // TODO: doesn't work for char
  def "multi-declaration with all primitive types in presence of cleanup-block"() {
    setup:
    def (byte b, int i, long l, float f, double d, boolean bool) =
    [(byte) 1, 1, 1l, 1f, 1d, true]

    expect:
    b == (byte) 1
    i == 1
    l == 1l
    f == 1f
    d == 1d
    bool

    cleanup:
    assert b == (byte) 1
    assert i == 1
    assert l == 1l
    assert f == 1f
    assert d == 1d
    assert bool
  }

  @Issue("https://github.com/spockframework/spock/issues/1266")
  def "cleanup blocks don't destroy method reference when invocation is assigned to variable with the same name"() {
    when:
    def foobar = foobar()

    then:
    consume(foobar)

    cleanup:
    foobar.size()
  }

  @Issue("https://github.com/spockframework/spock/issues/1332")
  def "cleanup blocks don't destroy method reference when invocation is assigned to a multi-assignment with the same name"() {
    when:
    def (foobar, b) = foobar()

    then:
    consume(foobar)

    cleanup:
    foobar.size()
  }

  void consume(def value) {

  }

  def foobar() {
    return ["foo", "bar"]
  }
}
