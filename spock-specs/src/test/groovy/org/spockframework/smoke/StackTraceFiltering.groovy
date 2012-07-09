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
import spock.lang.Issue
import spock.lang.Unroll
import org.spockframework.util.Identifiers
import org.spockframework.runtime.WrongExceptionThrownError

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
apackage.ASpec|a feature|1
    """)
  }

  def "unsatisfied explicit condition"() {
    when:
    runner.runFeatureBody """
expect: assert false
    """

    then:
    ConditionNotSatisfiedError e = thrown()
    stackTraceLooksLike(e, """
apackage.ASpec|a feature|1
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
$chain\$StaticInner|staticInner|46
$chain\$Inner|inner|40
$chain|b|30
$chain|a|26
apackage.ASpec|a feature|2
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
apackage.ASpec|a feature_closure1|-
apackage.ASpec|a feature|3
    """
  }

  def "exception in closure in field initializer"() {
    when:
    runner.runSpecBody """
def x = { assert false }()

def foo() { expect: true }
    """

    then:
    ConditionNotSatisfiedError e = thrown()
    // no idea why setup_closure1 appears twice
    stackTraceLooksLike e, """
apackage.ASpec|\$spock_initializeFields_closure1|-
apackage.ASpec|\$spock_initializeFields_closure1|-
apackage.ASpec|\$spock_initializeFields|1
    """
  }

  @Issue("http://issues.spockframework.org/detail?id=80")
  def "stack trace is truncated below invocation of fixture method"() {
    when:
    runner.runSpecBody """
def ${fixtureMethod}() {
  assert false
}

def feature() {
  expect: true
}
    """

    then:
    ConditionNotSatisfiedError e = thrown()
    stackTraceLooksLike e, """
apackage.ASpec|$fixtureMethod|-
    """

    where:
    fixtureMethod << Identifiers.FIXTURE_METHODS
  }

  @Issue("http://issues.spockframework.org/detail?id=75")
  def "feature method names are also restored for base specs"() {
    when:
    runner.runWithImports """
abstract class Base extends Specification {
  def "let me fail"() {
    expect: false
  }
}

class Derived extends Base {}
    """

    then:
    ConditionNotSatisfiedError e = thrown()
    stackTraceLooksLike e, """
apackage.Base|let me fail|3
    """
  }

  @Issue("http://issues.spockframework.org/detail?id=33")
  def "when creation of data provider fails, stack trace points to corresponding parameterization"() {
    when:
    runner.runSpecBody """
def foo() {
  expect:
  true

  where:
  x << [1]
  y << { throw new RuntimeException() }()
  z << [1]
}
    """

    then:
    RuntimeException e = thrown()
    stackTraceLooksLike e, """
apackage.ASpec|foo_closure1|7
apackage.ASpec|foo_closure1|-
apackage.ASpec|foo|7

    """
  }

  @Issue("http://issues.spockframework.org/detail?id=33")
  def "when data processor fails, stack trace source position points to corresponding parameterization"() {
    when:
    runner.runSpecBody """
def foo(int x, Class y, int z) {
  expect:
  true

  where:
  x << [1]
  $parameterization
  z << [1]
}
    """

    then:
    RuntimeException e = thrown()
    stackTraceLooksLike e, """
apackage.ASpec|foo|7
    """

    where:
    parameterization << ["y << [1]", "y = 1", "[_, y, _] << [[1], [1], [1]]"]
  }

  @Issue("http://issues.spockframework.org/detail?id=90")
  def "stack trace for explicit condition in setup block has line number"() {
    when:
    runner.runSpecBody """
def setup() {
  assert 1 > 2
}

def foo() { expect: true }
    """

    then:
    ConditionNotSatisfiedError e = thrown()
    e.stackTrace[0].lineNumber == 2
  }

  @Issue("http://issues.spockframework.org/detail?id=90")
  def "stack trace for nested explicit condition has line number"() {
    when:
    runner.runFeatureBody """
setup:
1.times { assert 1 > 2 }
    """

    then:
    ConditionNotSatisfiedError e = thrown()
    e.stackTrace[0].lineNumber == 2
  }
  
  @Issue("http://issues.spockframework.org/detail?id=156")
  def "causes get filtered as well"() {
    when:
    runner.runFeatureBody """
when:
throw new IOException()

then:
thrown(RuntimeException)
    """   
    
    then:
    WrongExceptionThrownError e = thrown()

    stackTraceLooksLike e, """
spock.lang.SpecInternals|thrownImpl|-
apackage.ASpec|a feature|5
    """
    
    stackTraceLooksLike e.cause, """
apackage.ASpec|a feature|2
    """
  }
}