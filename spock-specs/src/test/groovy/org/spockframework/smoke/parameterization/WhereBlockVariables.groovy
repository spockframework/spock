/*
 * Copyright 2026 the original author or authors.
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

package org.spockframework.smoke.parameterization

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.GroovyRuntimeUtil
import spock.lang.Issue
import spock.lang.Requires
import spock.lang.ResourceLock

@Issue("https://github.com/spockframework/spock/issues/138")
class WhereBlockVariables extends EmbeddedSpecification {
  def "final where-block variable can be used in data table cells"() {
    when:
    runner.runFeatureBody '''
expect:
input == expected

where:
final sep = "/"
input              | expected
"a${sep}b"         | "a/b"
"x${sep}y${sep}z"  | "x/y/z"
'''

    then:
    noExceptionThrown()
  }

  def "final where-block variable is evaluated once and reused across providers"() {
    when:
    runner.runFeatureBody '''
expect:
a.is(b)            // same instance in every iteration => single evaluation

where:
final marker = new Object()
a << [marker, marker]
b << [marker, marker]
'''

    then:
    noExceptionThrown()
  }

  def "final where-block variable can be used in a derived data variable"() {
    when:
    runner.runFeatureBody '''
expect:
greeting == expected

where:
final prefix = "Hello, "
name     | expected
"world"  | "Hello, world"
"spock"  | "Hello, spock"

greeting = prefix + name
'''

    then:
    noExceptionThrown()
  }

  def "where-block variable is not visible in the feature body"() {
    when:
    runner.runFeatureBody '''
expect:
sep == "/"          // `sep` is a where-block local, unbound here

where:
final sep = "/"
x << [1, 2]
'''

    then:
    thrown(Throwable)
  }

  def "where-block variable initializer can read a @Shared field"() {
    when:
    runner.runSpecBody '''
@Shared
String base = "abc"

def feature() {
  expect:
  value == "abc/x"

  where:
  final sep = base + "/"
  value = sep + "x"
}
'''

    then:
    noExceptionThrown()
  }

  def "final where-block variable works with combined data providers"() {
    when:
    runner.runFeatureBody '''
expect:
a.endsWith("-")
b in [1, 2]

where:
final sep = "-"
a << ["x${sep}", "y${sep}"]
combined:
b << [1, 2]
'''

    then:
    noExceptionThrown()
  }

  @Requires(value = { GroovyRuntimeUtil.MAJOR_VERSION >= 3 }, reason = "'final (a, b) = ...' is only parseable by the Parrot parser (Groovy 3.0+)")
  def "final multiple-assignment where-block variables can be used"() {
    when:
    runner.runFeatureBody '''
expect:
value == expected

where:
final (lo, hi) = [1, 10]
value | expected
lo    | 1
hi    | 10
'''

    then:
    noExceptionThrown()
  }

  @Requires(value = { GroovyRuntimeUtil.MAJOR_VERSION >= 3 }, reason = "'final (a, b) = ...' is only parseable by the Parrot parser (Groovy 3.0+)")
  def "final multiple-assignment where-block variables are evaluated once and reused across providers"() {
    when:
    runner.runFeatureBody '''
expect:
a.is(b)            // both providers see the same instances => single evaluation

where:
final (first, second) = [new Object(), new Object()]
a << [first, second]
b << [first, second]
'''

    then:
    noExceptionThrown()
  }

  def "a where-block variable can reference an earlier where-block variable"() {
    when:
    runner.runFeatureBody '''
expect:
value == "Hello, World!"

where:
final greeting = "Hello"
final subject = "World"
final message = "${greeting}, ${subject}!"

value << [message, message]
'''

    then:
    noExceptionThrown()
  }

  def "a where-block variable closure can capture another where-block variable"() {
    when:
    runner.runFeatureBody '''
expect:
result == 15

where:
final base = 5
final adder = { it + base }

value << [10, 10]
result = adder(value)
'''

    then:
    noExceptionThrown()
  }

  def "a where-block variable can be the source of data pipes"() {
    when:
    runner.runFeatureBody '''
expect:
user in ["alice", "bob"]
password in ["secret", "hunter2"]

where:
final factory = new Object() {
  List<String> users() { ["alice", "bob"] }
  List<String> passwords() { ["secret", "hunter2"] }
}

user << factory.users()
password << factory.passwords()
'''

    then:
    noExceptionThrown()
  }

  def "filter-block can reference a where-block variable"() {
    when:
    def result = runner.runFeatureBody '''
expect:
true

where:
final threshold = 2
x << [1, 2, 3]

filter:
x >= threshold
'''

    then:
    // only x == 2 and x == 3 survive the filter; counts include the feature container
    result.testsAbortedCount == 0
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsStartedCount == 3
    result.testsSucceededCount == 3
  }

  def "a failing where-block variable initializer fails the feature with the original exception"() {
    when:
    runner.runFeatureBody '''
expect:
true

where:
final boom = { throw new IllegalStateException("initializer failed") }()
x << [1, 2]
'''

    then:
    IllegalStateException e = thrown()
    e.message == "initializer failed"
  }

  @ResourceLock("RecordingCloseable.closed")
  def "an AutoCloseable where-block variable is closed once after the feature completes"() {
    given:
    RecordingCloseable.closed.clear()
    runner.addClassImport(RecordingCloseable)

    when:
    def result = runner.runFeatureBody '''
expect:
name == "closed-once"

where:
final resource = new RecordingCloseable("closed-once")
value << [1, 2, 3]
name = resource.name
'''

    then:
    result.testsFailedCount == 0
    // closed exactly once, after all iterations, not once per iteration
    RecordingCloseable.closed == ["closed-once"]
  }

  @ResourceLock("RecordingCloseable.closed")
  def "AutoCloseable where-block variables are closed in reverse declaration order"() {
    given:
    RecordingCloseable.closed.clear()
    runner.addClassImport(RecordingCloseable)

    when:
    runner.runFeatureBody '''
expect:
names == "lifo-first,lifo-second"

where:
final first = new RecordingCloseable("lifo-first")
final second = new RecordingCloseable("lifo-second")
value << [1, 2]
names = "${first.name},${second.name}"
'''

    then:
    RecordingCloseable.closed == ["lifo-second", "lifo-first"]
  }

  @ResourceLock("RecordingCloseable.closed")
  def "a failure while closing a where-block variable is swallowed"() {
    given:
    RecordingCloseable.closed.clear()
    runner.addClassImport(RecordingCloseable)
    runner.addClassImport(ThrowingCloseable)

    when:
    def result = runner.runFeatureBody '''
expect:
value != null

where:
final bad = new ThrowingCloseable()
final ok = new RecordingCloseable("swallow-ok")
value << [1, 2]
marker = bad.hashCode() + ok.name
'''

    then:
    result.testsFailedCount == 0
    // 'ok' is declared last, so it is closed first and recorded even though 'bad' throws afterwards
    RecordingCloseable.closed == ["swallow-ok"]
  }

  def "a where-block variable that is not AutoCloseable is left untouched"() {
    given:
    PlainResource.closeCalled = false
    runner.addClassImport(PlainResource)

    when:
    runner.runFeatureBody '''
expect:
tag != null

where:
final resource = new PlainResource()
value << [1, 2]
tag = resource.toString()
'''

    then:
    !PlainResource.closeCalled
  }
}

// features touching the shared 'closed' list must declare
// @ResourceLock("RecordingCloseable.closed"), spock-specs runs with parallel execution
class RecordingCloseable implements AutoCloseable {
  static final List<String> closed = [].asSynchronized()
  final String name

  RecordingCloseable(String name) {
    this.name = name
  }

  @Override
  void close() {
    closed << name
  }
}

class ThrowingCloseable implements AutoCloseable {
  @Override
  void close() {
    throw new IllegalStateException("close failed")
  }
}

class PlainResource {
  static boolean closeCalled = false

  // has a close() method but does not implement AutoCloseable, so it must not be auto-closed
  void close() {
    closeCalled = true
  }
}
