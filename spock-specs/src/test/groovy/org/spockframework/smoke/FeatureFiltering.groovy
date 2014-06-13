
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

package org.spockframework.smoke

import org.junit.runner.manipulation.Filter
import org.junit.runner.Request

import org.spockframework.EmbeddedSpecification
import spock.lang.Issue

class FeatureFiltering extends EmbeddedSpecification {
    def "filter selected feature methods"() {
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

  def "filtering all feature methods results in exception"() {
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

  @Issue("http://issues.spockframework.org/detail?id=76")
  def "filtering across inheritance chain"() {
    def derived = compiler.compileWithImports("""
abstract class Base extends Specification {
  def feature1() { expect: true }
  def feature2() { expect: true }
}

class Derived extends Base {
  def feature3() { expect: true }
  def feature4() { expect: true }
}
    """).find { it.simpleName == "Derived" }

    def request = Request.aClass(derived).filterWith(
        [shouldRun: { desc -> desc.methodName in ["feature1", "feature3"] }, describe: { "xxx" }] as Filter)

    when:
    def result = runner.runRequest(request)

    then:
    result.runCount == 2
    result.failureCount == 0
    result.ignoreCount == 0
  }
}
