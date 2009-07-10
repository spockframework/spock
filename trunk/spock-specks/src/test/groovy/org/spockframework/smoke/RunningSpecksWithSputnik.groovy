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
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Filter
import spock.lang.*
import org.junit.runner.notification.RunListener

@Speck
@RunWith(Sputnik)
public class RunningSpecksWithSputnik {
  def runner = new EmbeddedSpeckRunner()
  
  @Issue("13")
  def "failing setupSpeck method"() {
    runner.throwFailure = false

    when:
    def result = runner.runSpeckBody("""
def setupSpeck() { throw new Exception() }
def feature() { expect: true }
    """)

    then:
    result.runCount == 0 // we don't currently call notifier.fireTestStarted()/fireTestFinished() for setupSpeck()
    result.failureCount == 1
    result.ignoreCount == 0
  }

  def "failing cleanupSpeck method"() {
    runner.throwFailure = false

    when:
    def result = runner.runSpeckBody("""
def cleanupSpeck() { throw new Exception() }
def feature() { expect: true }
    """)

    then:
    result.runCount == 1 // we don't currently call notifier.fireTestStarted()/fireTestFinished() for cleanupSpeck()
    result.failureCount == 1
    result.ignoreCount == 0
  }

  def "ignoring a Speck"() {
    def compiler = new EmbeddedSpeckCompiler()
    def clazz = compiler.compileWithImports("""
@Speck
@RunWith(Sputnik)
@Ignore
class Foo {
  static log = ""
  def feature1() { setup: log += "1" }
  def feature2() { setup: log += "2" }
}
    """)

    when:
    def result = runner.runClass(clazz)

    then:
    clazz.log == ""
    result.runCount == 0
    result.failureCount == 0
    result.ignoreCount == 1 // one Speck
  }

  @Issue("20")
  def "ignoring feature methods"() {
    def compiler = new EmbeddedSpeckCompiler()
    def clazz = compiler.compileSpeckBody("""
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
    def compiler = new EmbeddedSpeckCompiler()
    def clazz = compiler.compileSpeckBody("""
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
    def compiler = new EmbeddedSpeckCompiler()
    def clazz = compiler.compileSpeckBody("""
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
    def compiler = new EmbeddedSpeckCompiler()
    def clazz = compiler.compileSpeckBody("""
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
    def result = runner.runSpeckBody("""
@Unroll
def 'foo bar'() {
  expect:
  a == b

  where:
  a << [1, 2, 3]
  b << [1, 0, 3]
}
    """)

    then:
    1 * listener.testRunStarted(_)
    1 * listener.testRunFinished(_)
    3 * listener.testStarted({ it.displayName.startsWith "foo bar[" })
    3 * listener.testFinished({ it.displayName.startsWith "foo bar[" })
    1 * listener.testFailure({ it.description.displayName.startsWith "foo bar[1]"})
    0 * listener._

    result.runCount == 3
    result.failureCount == 1
    result.ignoreCount == 0
  }
}