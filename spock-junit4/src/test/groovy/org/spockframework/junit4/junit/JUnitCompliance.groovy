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

package org.spockframework.junit4.junit


import spock.lang.Issue
import spock.util.EmbeddedSpecCompiler

class JUnitCompliance extends JUnitBaseSpec {
  @Issue("http://issues.spockframework.org/detail?id=13")
  def "failing setupSpec method"() {
    runner.throwFailure = false

    when:
    def result = runner.runSpecBody("""
def setupSpec() { throw new Exception() }
def feature() { expect: true }
    """)

    then:
    result.containersFailedCount == 1
  }

  def "failing cleanupSpec method"() {
    runner.throwFailure = false

    when:
    def result = runner.runSpecBody("""
def cleanupSpec() { throw new Exception() }
def feature() { expect: true }
    """)

    then:
    result.containersFailedCount == 1
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
    result.containersSkippedCount == 1
  }

  @Issue("http://issues.spockframework.org/detail?id=20")
  def "ignoring feature methods"() {
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
    result.testsSucceededCount == 1
    result.testsSkippedCount == 2
  }
}
