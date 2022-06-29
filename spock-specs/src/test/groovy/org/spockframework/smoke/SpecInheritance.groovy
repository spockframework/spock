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

package org.spockframework.smoke

import org.spockframework.EmbeddedSpecification

import spock.lang.*
import org.spockframework.compiler.InvalidSpecCompileException

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

    def derived = classes.find { it.simpleName == "Derived" }

    when:
    runner.runClass(derived)

    then:
    derived.log == ["ss1", "ss2", "s1", "s2", "c2", "c1", "cs2", "cs1"]
  }

  def "cannot call super method from fixture method"() {
    when:
    compiler.compileWithImports("""
class Base extends Specification {
  def $method() {}
}

class Derived extends Base {
  def $method() {
    super.$method()
  }

  def feature() {
    expect: true
  }
}
    """)

    then:
    thrown(InvalidSpecCompileException)

    where:
    method << ["setup", "cleanup", "setupSpec", "cleanupSpec"]
  }

  def "feature methods are run in correct order"() {
    def classes = compiler.compileWithImports("""
class Base extends Specification {
  static log = []

  def "1"() {
    log << 1
    expect: true
  }

  def "2"() {
    log << 2
    expect: true
  }
}

class Derived extends Base {
  def "3"() {
    log << 3
    expect: true
  }

  def "4"() {
    log << 4
    expect: true
  }
}
    """)

    def derived = classes.find { it.simpleName == "Derived" }

    when:
    runner.runClass(derived)

    then:
    derived.log == [1, 2, 3, 4]
  }

  def "exception in base class fixture method causes failure"() {
    def classes = compiler.compileWithImports("""
class Base extends Specification {
  def ${fixtureMethod}() { throw new IOException() }
}

class Derived extends Base {
  def ${fixtureMethod}() {}

  def feature() { expect: true }
}
    """)

    def derived = classes.find { it.simpleName == "Derived" }

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
  def f1() {
    expect: true
  }

  def f2() {
    expect: false
  }

  @Ignore
  def f3() {
    expect: false
  }
}

class Derived extends Base {
  def f4() {
    expect: false
  }

  def f5() {
    expect: true
  }
}
    """)

    def derived = classes.find { it.simpleName == "Derived" }
    runner.throwFailure = false

    when:
    def result = runner.runClass(derived)

    then:
    result.testsStartedCount == 4
    result.testsSucceededCount == 2
    result.testsFailedCount == 2
    result.testsSkippedCount == 1
  }

  @Issue("https://github.com/spockframework/spock/issues/175")
  def "feature methods cannot be overridden"() {
    def classes = compiler.compileWithImports("""
class Base extends Specification {
  static log = []

  def foo() {
    setup: log << 1
  }
}

class Derived extends Base {
  def foo() {
    setup: log << 2
  }
}
    """)

    def derived = classes.find { it.simpleName == "Derived" }

    when:
    def result = runner.runClass(derived)

    then:
    derived.log == [1, 2]
    result.testsSucceededCount == 2
  }

  def "private feature methods with same name can coexist peacefully in inheritance chain"() {
    def classes = compiler.compileWithImports("""
class Base extends Specification {
  static log = []

  private foo() {
    setup: log << 1
  }
}

class Derived extends Base {
  private foo() {
    setup: log << 2
  }
}
    """)

    def derived = classes.find { it.simpleName == "Derived" }

    when:
    def result = runner.runClass(derived)

    then:
    derived.log == [1, 2]
    result.testsSucceededCount == 2
  }

  def "can invoke super.helperMethod()"() {
    def classes = compiler.compileWithImports("""
class Base extends Specification {
  def helperMethod() {
    return "helper invoked"
  }
}

class Derived extends Base {
  def helperMethod() {
    super.helperMethod()
  }

  def feature() {
    expect: helperMethod() == "helper invoked"
  }
}
    """)

    def derived = classes.find { it.simpleName == "Derived" }

    when:
    runner.runClass(derived)

    then:
    noExceptionThrown()
  }
}

