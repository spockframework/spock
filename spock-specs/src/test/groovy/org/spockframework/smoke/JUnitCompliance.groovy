/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke

import org.junit.runner.Request
import org.junit.runner.manipulation.Filter
import org.junit.runner.notification.RunListener
import org.spockframework.EmbeddedSpecification
import spock.lang.Issue
import spock.util.EmbeddedSpecCompiler

public class JUnitCompliance extends EmbeddedSpecification {
  @Issue("http://issues.spockframework.org/detail?id=13")
  def "failing setupSpec method"() {
    runner.throwFailure = false

    when:
    def result = runner.runSpecBody("""
def setupSpec() { throw new Exception() }
def feature() { expect: true }
    """)

    then:
    result.runCount == 0 // we don't currently call notifier.fireTestStarted()/fireTestFinished() for setupSpec()
    result.failureCount == 1
    result.ignoreCount == 0

    def desc = result.failures[0].description
    desc.isSuite() // failure description is description of the test class
    desc.className == "apackage.ASpec"
  }

  def "failing cleanupSpec method"() {
    runner.throwFailure = false

    when:
    def result = runner.runSpecBody("""
def cleanupSpec() { throw new Exception() }
def feature() { expect: true }
    """)

    then:
    result.runCount == 1 // we don't currently call notifier.fireTestStarted()/fireTestFinished() for cleanupSpec()
    result.failureCount == 1
    result.ignoreCount == 0
  }

  def "ignoring a Spec"() {
    def compiler = new EmbeddedSpecCompiler()
    def clazz = compiler.compileWithImports("""
@Ignore
class Foo extends Specification {
  static log = "" // needs to be static rather than @Shared s.t. we can access it from outside
  def feature1() { setup: log += "1" }
  def feature2() { setup: log += "2" }
}
    """)[0]

    when:
    def result = runner.runClass(clazz)

    then:
    clazz.log == ""
    result.runCount == 0
    result.failureCount == 0
    result.ignoreCount == 1 // one Spec
  }

  @Issue("http://issues.spockframework.org/detail?id=20")
  def "ignoring feature methods"() {
    def compiler = new EmbeddedSpecCompiler()
    def clazz = compiler.compileSpecBody("""
static log = ""
@Ignore
def feature1() { setup: log += "1" }
def feature2() { setup: log += "2" }
@Ignore
def feature3() { setup: log += "3" }

    """)

    when:
    def result = runner.runClass(clazz)

    then:
    clazz.log == "2"
    result.runCount == 1
    result.failureCount == 0
    result.ignoreCount == 2
  }

  def "sorting feature methods"() {
    def compiler = new EmbeddedSpecCompiler()
    def clazz = compiler.compileSpecBody("""
static log = ""
def feature1() { setup: log += "1" }
def feature2() { setup: log += "2" }
def feature3() { setup: log += "3" }
    """)

    def request = Request.aClass(clazz).sortWith(
        { desc1, desc2 -> desc2.methodName.compareTo(desc1.methodName) } as Comparator)

    when:
    def result = runner.runRequest(request)

    then:
    clazz.log == "321"
    result.runCount == 3
    result.failureCount == 0
    result.ignoreCount == 0
  }

  def "filtering feature methods"() {
    def compiler = new EmbeddedSpecCompiler()
    def clazz = compiler.compileSpecBody("""
static log = ""
def feature1() { setup: log += "1" }
def feature2() { setup: log += "2" }
def feature3() { setup: log += "3" }
    """)

    def request = Request.aClass(clazz).filterWith(
        [shouldRun: { desc -> desc.methodName == "feature2" }, describe: { "feature2" }] as Filter)

    when:
    def result = runner.runRequest(request)

    then:
    clazz.log == "2"
    result.runCount == 1
    result.failureCount == 0
    result.ignoreCount == 0
  }

  def "filtering all feature methods"() {
    def compiler = new EmbeddedSpecCompiler()
    def clazz = compiler.compileSpecBody("""
static log = ""
def feature1() { setup: log += "1" }
def feature2() { setup: log += "2" }
def feature3() { setup: log += "3" }
    """)

    def request = Request.aClass(clazz).filterWith([shouldRun: { false }, describe: { "xxx" }] as Filter)

    when:
    runner.runRequest(request)

    then:
    Exception e = thrown()
    e.message.contains "xxx"
    clazz.log == ""
  }

  def "running a data-driven feature"() {
    runner.throwFailure = false
    RunListener listener = Mock()
    runner.listeners << listener

    when:
    def result = runner.runSpecBody("""
def "foo"() {
  expect:
  a == b

  where:
  a << [1, 2, 3]
  b << [2, 1, 3]
}
    """)

    then:
    1 * listener.testRunStarted(_)
    1 * listener.testRunFinished(_)
    1 * listener.testStarted { it.methodName == "foo" }
    1 * listener.testFinished { it.methodName == "foo" }
    2 * listener.testFailure { it.description.methodName == "foo" }
    0 * listener._

    result.runCount == 1
    result.failureCount == 2
    result.ignoreCount == 0
  }

  def "testStarted/testFinished not called for @Ignore'd spec method"() {
    RunListener listener = Mock()
    runner.listeners << listener

    when:
    runner.runSpecBody """
@Ignore
def foo() {
  expect: true
}
    """

    then:
    1 * listener.testIgnored(_)
    0 * listener.testStarted(_)
    0 * listener.testFinished(_)
  }

  def "testStarted/testFinished not called for spec methods of @Ignore'd spec"() {
    RunListener listener = Mock()
    runner.listeners << listener

    when:
    runner.runWithImports """
@Ignore
class Foo extends Specification {
  def foo() {
    expect: true
  }

  def bar() {
    expect: true
  }
}
    """

    then:
    1 * listener.testIgnored(_)
    0 * listener.testStarted(_)
    0 * listener.testFinished(_)
  }
}