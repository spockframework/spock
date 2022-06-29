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

package org.spockframework.smoke

import org.junit.platform.engine.discovery.DiscoverySelectors

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

    def request = DiscoverySelectors.selectMethod(clazz, "feature2")

    when:
    def result = runner.runWithSelectors(request)

    then:
    clazz.log == "2"
    result.testsSucceededCount == 1
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }

  def "filtering all feature methods results in empty result"() {
    def clazz = compiler.compileSpecBody("""
static log = ""
def feature1() { setup: log += "1" }
def feature2() { setup: log += "2" }
def feature3() { setup: log += "3" }
    """)

    def request = DiscoverySelectors.selectMethod(clazz, "xxx")

    when:
    def result = runner.runWithSelectors(request)

    then:
    noExceptionThrown()
    result.testsFoundCount == 0
  }

  @Issue("https://github.com/spockframework/spock/issues/198")
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

    when:
    def result = runner.runWithSelectors(DiscoverySelectors.selectMethod(derived, "feature1"), DiscoverySelectors.selectMethod(derived, "feature3"))

    then:
    result.testsSucceededCount == 2
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }
}
