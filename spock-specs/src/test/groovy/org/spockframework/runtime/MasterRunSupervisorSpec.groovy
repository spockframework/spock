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

package org.spockframework.runtime

import org.opentest4j.MultipleFailuresError
import org.opentest4j.TestAbortedException
import org.spockframework.runtime.condition.IObjectRenderer
import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.model.IErrorContext
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Issue
import spock.lang.Specification

@Issue("https://github.com/spockframework/spock/issues/2175")
class MasterRunSupervisorSpec extends Specification {
  IRunListener listener = Mock()
  IStackTraceFilter filter = Stub()
  IObjectRenderer<Object> renderer = Stub()
  MethodInfo method = new MethodInfo()
  IErrorContext errorContext = Stub()
  ErrorInfoCollector collector = new ErrorInfoCollector()
  SpecInfo spec = new SpecInfo().tap { addListener(listener) }
  MasterRunSupervisor supervisor = new MasterRunSupervisor(spec, filter, renderer)

  def "the public aggregate constructor still accepts a null heading"() {
    given:
    def child = new AssertionError("child")

    when:
    def failure = new SpockMultipleFailuresError(null, [child])

    then:
    failure.message.startsWith("Multiple Failures (1 failure)")
    failure.failures == [child]
  }

  def "preserves aggregate diagnostics and notifies listeners of each leaf"() {
    given:
    def first = new AssertionError("first")
    def second = new IllegalStateException("second")
    def original = new MultipleFailuresError("Original heading", [first, second])
    def cause = new IOException("cause")
    def suppressed = new IOException("suppressed")
    original.initCause(cause)
    original.addSuppressed(suppressed)
    def trace = [new StackTraceElement("Extension", "intercept", "Extension.java", 42)] as StackTraceElement[]
    original.stackTrace = trace

    when:
    supervisor.error(collector, new ErrorInfo(method, original, errorContext))
    collector.assertEmpty()

    then:
    MultipleFailuresError failure = thrown()
    failure.message == original.message
    failure.stackTrace == trace
    failure.cause.is(cause)
    failure.suppressed.toList() == [suppressed]
    failure.failures == [first, second]
    1 * listener.error({ ErrorInfo it ->
      it.exception.is(first) && it.method.is(method) && it.errorContext.is(errorContext)
    })
    1 * listener.error({ ErrorInfo it ->
      it.exception.is(second) && it.method.is(method) && it.errorContext.is(errorContext)
    })
    0 * listener._
  }

  def "preserves nested external aggregates while expanding internal condition groups"() {
    given:
    def first = new AssertionError("first")
    def second = new AssertionError("second")
    def nested = new MultipleFailuresError("Nested heading", [first])
    def internal = new SpockMultipleFailuresError("", [nested, second])
    def original = new MultipleFailuresError("Outer heading", [internal])

    when:
    supervisor.error(collector, new ErrorInfo(method, original, errorContext))
    collector.assertEmpty()

    then:
    MultipleFailuresError failure = thrown()
    failure.message == original.message
    failure.failures.size() == 2
    with(failure.failures[0], MultipleFailuresError) {
      message == nested.message
      stackTrace == nested.stackTrace
      failures == [first]
    }
    failure.failures[1].is(second)
    1 * listener.error({ it.exception.is(first) })
    1 * listener.error({ it.exception.is(second) })
    0 * listener._
  }

  def "does not discard an aggregate with a single aborted failure"() {
    given:
    def aborted = new TestAbortedException("aborted")
    def original = new MultipleFailuresError("Aggregate heading", [aborted])

    when:
    supervisor.error(collector, new ErrorInfo(method, original, errorContext))
    collector.assertEmpty()

    then:
    MultipleFailuresError failure = thrown()
    failure.message == original.message
    failure.stackTrace == original.stackTrace
    failure.failures == [aborted]
    0 * listener._
  }

  def "does not flatten a preserved aggregate when it is collected again"() {
    given:
    def first = new AssertionError("first")
    def second = new AssertionError("second")
    def original = new MultipleFailuresError("Original heading", [first, second])

    when:
    supervisor.error(collector, new ErrorInfo(method, original, errorContext))
    collector.assertEmpty()

    then:
    MultipleFailuresError transformed = thrown()

    when:
    def nextCollector = new ErrorInfoCollector()
    supervisor.error(nextCollector, new ErrorInfo(method, transformed, errorContext))
    nextCollector.assertEmpty()

    then:
    MultipleFailuresError failure = thrown()
    failure.message == original.message
    failure.stackTrace == original.stackTrace
    failure.failures == [first, second]
  }
}
