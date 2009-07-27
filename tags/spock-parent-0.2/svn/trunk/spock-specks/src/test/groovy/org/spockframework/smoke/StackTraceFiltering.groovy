/*
 * Copyright 2008 the original author or authors.
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

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.smoke.CallChainException
import spock.lang.Issue
import spock.lang.Unroll

/**
 * @author Peter Niederwieser
 */
@Issue("http://issues.spockframework.org/detail?id=21")
class StackTraceFiltering extends EmbeddedSpecification {
  def "unsatisfied implicit condition"() {
    when:
    runner.runFeatureBody """
expect: false
    """

    then:
    ConditionNotSatisfiedError e = thrown()
    stackTraceLooksLike(e, """
apackage.ASpeck|a feature|1
    """)
  }

  @Unroll("exception in #displayName")
  def "exception in call chain"() {
    when:
    runner.runFeatureBody """
setup:
new $chain().a()
    """

    then:
    CallChainException e = thrown()
    stackTraceLooksLike(e, """
$chain|c|35
$chain|b|30
$chain|a|26
apackage.ASpeck|a feature|2
    """)

    where:
    chain << ["org.spockframework.smoke.GroovyCallChain", "org.spockframework.smoke.JavaCallChain"]
    displayName << ["Groovy call chain", "Java call chain"]
  }

  def "exception in closure"() {
    when:
    runner.runFeatureBody """
setup:
def x // need some statement between label and closure (otherwise Groovy would consider the following a block)
{ -> assert false }()
    """

    then:
    ConditionNotSatisfiedError e = thrown()
    stackTraceLooksLike e, """
apackage.ASpeck|a feature@closure1|-
apackage.ASpeck|a feature|3
    """
  }

  def "exception in closure in field initializer"() {
    when:
    runner.runSpeckBody """
def x = { assert false }()

def foo() { expect: true }
    """

    then:
    ConditionNotSatisfiedError e = thrown()
  }

  private void stackTraceLooksLike(Throwable exception, String template) {
    def trace = exception.stackTrace
    def lines = template.trim().split("\n")
    assert trace.size() == lines.size()

    lines.eachWithIndex { line, index ->
      def parts = line.split("\\|")
      def traceElem = trace[index]
      assert traceElem.className == parts[0]
      assert traceElem.methodName == parts[1]
      assert parts[2] == "-" || traceElem.lineNumber == parts[2] as int
    }
  }
}