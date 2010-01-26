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

package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.InvalidSpecException
import spock.lang.Issue

/**
 * Spock's @Ignore semantics are the same as JUnit's.
 * 
 * @author Peter Niederwieser
 */
@Issue("http://issues.spockframework.org/detail?id=12")
class IgnoreExtension extends EmbeddedSpecification {
  def "ignore spec"() {
    when:
    def result = runner.runWithImports("""
@Ignore
class Foo extends Specification {
  def foo() {
    expect: false
  }

  def bar() {
    expect: false
  }
}
    """)

    then:
    result.runCount == 0
    result.failureCount == 0
    result.ignoreCount == 1
  }

  def "ignored feature methods"() {
    when:
    def result = runner.runSpecBody("""
@Ignore
def "ignored"() {
  expect: false
}

def "not ignored"() {
  expect: true
}

@Ignore
def "also ignored"() {
  expect: false
}
    """)

    then:
    result.runCount == 1
    result.failureCount == 0
    result.ignoreCount == 2
  }

  def "fixture methods cannot be ignored"() {
    when:
    runner.runSpecBody """
@Ignore
def setup() {}
    """

    then:
    thrown(InvalidSpecException)
  }

  def "ignore methods in base and derived class"() {
    def classes = compiler.compileWithImports("""
class Base extends Specification {
  @Ignore
  def feature1() {
    expect: false
  }

  def feature2() {
    expect: true
  }
}

class Derived extends Base {
  def feature3() {
    expect: true
  }

  @Ignore
  def feature4() {
    expect: false
  }
}
    """)

    def derived = classes.find { it.simpleName == "Derived" }

    when:
    def result = runner.runClass(derived)


    then:
    result.runCount == 2
    result.failureCount == 0
    result.ignoreCount == 2
  }

  def "ignoring base class has no effect on running derived class"() {
    def classes = compiler.compileWithImports("""
@Ignore
class Base extends Specification {
  def feature1() {
    expect: true
  }
}

class Derived extends Base {
  def feature2() {
    expect: true
  }
}
    """)

    def derived = classes.find { it.simpleName == "Derived" }

    when:
    def result = runner.runClass(derived)


    then:
    result.runCount == 2
    result.failureCount == 0
    result.ignoreCount == 0
  }
}



