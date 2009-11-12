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

import org.spockframework.EmbeddedSpecification

class SpecInheritance extends EmbeddedSpecification {
  def "fixture methods are run in correct order"() {
    def classes = compiler.compileWithImports("""
class Base extends Specification {
  static log = []

  def setupSpec() {
    log << "ss1"
  }

  def setup() {
    log << "s1"
  }

  def cleanup() {
    log << "c1"
  }

  def cleanupSpec() {
    log << "cs1"
  }
}

class Derived extends Base {
  def setupSpec() {
    log << "ss2"
  }

  def setup() {
    log << "s2"
  }

  def cleanup() {
    log << "c2"
  }

  def cleanupSpec() {
    log << "cs2"
  }

  def "some feature method"() {
    expect: true
  }
}
    """)

    def derived = classes.find { it.name.endsWith("Derived") }

    when:
    runner.runClass(derived)

    then:
    derived.log == ["ss1", "ss2", "s1", "s2", "c2", "c1", "cs2", "cs1"]
  }

  def "feature methods are run in correct order"() {
    def classes = compiler.compileWithImports("""
class Base extends Specification {
  static log = []

  def "1"() {
    log << "1"
    expect: true
  }

  def "2"() {
    log << "2"
    expect: true
  }
}

class Derived extends Base {
  def "3"() {
    log << "3"
    expect: true
  }

  def "4"() {
    log << "4"
    expect: true
  }
}
    """)

    def derived = classes.find { it.name.endsWith("Derived") }

    when:
    runner.runClass(derived)

    then:
    derived.log == ["1", "2", "3", "4"]
  }

  def "exception in base class fixture method causes failure"() {
    def classes = compiler.compileWithImports("""
class BaseSpec extends Specification {
  def ${fixtureMethod}() { throw new IOException() }
}

class DerivedSpec extends BaseSpec {
  def ${fixtureMethod}() {}

  def feature() { expect: true }
}
    """)

    def derived = classes.find { it.name.endsWith("DerivedSpec") }

    when:
    runner.runClass(derived)

    then:
    thrown(IOException)

    where:
    fixtureMethod << ["setup", "cleanup", "setupSpec", "cleanupSpec"]
  }

  def "junit reports correct run/failure/ignore counts"() {
    def classes = compiler.compileWithImports("""
class Base extends Specification {
  def "1"() {
    expect: true
  }

  def "2"() {
    expect: false
  }

  @Ignore
  def "3"() {
    expect: false
  }
}

class Derived extends Base {
  def "4"() {
    expect: false
  }

  def "5"() {
    expect: true
  }
}
    """)

    def derived = classes.find { it.name.endsWith("Derived") }
    runner.throwFailure = false
    
    when:
    def result = runner.runClass(derived)

    then:
    result.runCount == 4
    result.failureCount == 2
    result.ignoreCount == 1
  }
}

