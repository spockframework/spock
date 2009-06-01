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

import org.junit.runner.RunWith

import spock.lang.*
import static spock.lang.Predef.*
import org.junit.runner.JUnitCore
import org.junit.runner.Result
import org.spockframework.runtime.ConditionNotSatisfiedError

/**
 * @author Peter Niederwieser
 */
@Speck
@RunWith(Sputnik)
class StackTraceFiltering {
  def "unsatisfied implicit condition"() {
    def exception = runAndCaptureException("""
package abc

import spock.lang.*
import org.junit.runner.RunWith

@Speck
@RunWith(Sputnik)
class Foo {
  def "a feature method"() {
    expect: false  
  }
}
    """)

    expect:
    exception instanceof ConditionNotSatisfiedError
    filteredTraceLooksLike(exception, """
abc.Foo|a feature method|10
    """)
  }

  def "exception in #{callChain}"() {
    def exception = runAndCaptureException("""
package abc

import spock.lang.*
import org.junit.runner.RunWith
import org.spockframework.smoke.$callChain

@Speck
@RunWith(Sputnik)
class Foo {
  def "a feature method"() {
    when:
    new $callChain().a()

    then:
    true
  }
}
    """)

    expect:
    exception instanceof CallChainException
    filteredTraceLooksLike(exception, """
org.spockframework.smoke.$callChain|c|35
org.spockframework.smoke.$callChain|b|30
org.spockframework.smoke.$callChain|a|26
abc.Foo|a feature method|12
    """)

    where:
    callChain << ["GroovyCallChain", "JavaCallChain"]
  }

  private Throwable runAndCaptureException(String speck) {
    def clazz = new GroovyClassLoader().parseClass(speck.trim())
    Result result = JUnitCore.runClasses(clazz)

    assert result.failureCount == 1
    return result.failures[0].exception
  }

  private void filteredTraceLooksLike(Throwable exception, String template) {
    def trace = exception.stackTrace
    def lines = template.trim().split("\n")
    assert trace.size() == lines.size()

    lines.eachWithIndex { line, index ->
      def parts = line.split("\\|")
      def traceElem = trace[index]
      assert traceElem.className == parts[0]
      assert traceElem.methodName == parts[1]
      assert traceElem.lineNumber == parts[2] as int
    }
  }
}