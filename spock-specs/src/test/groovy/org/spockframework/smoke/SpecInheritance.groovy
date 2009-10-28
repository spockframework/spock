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
class BaseSpec extends Specification {
  static log = []

  def setupSpeck() {
    log << "ss1"
  }

  def setup() {
    log << "s1"
  }

  def cleanup() {
    log << "c1"
  }

  def cleanupSpeck() {
    log << "cs1"
  }
}

class DerivedSpec extends BaseSpec {
  def setupSpeck() {
    log << "ss2"
  }

  def setup() {
    log << "s2"
  }

  def cleanup() {
    log << "c2"
  }

  def cleanupSpeck() {
    log << "cs2"
  }

  def "some feature method"() {
    expect: true
  }
}
    """)

    def derived = classes.find { it.name.endsWith("DerivedSpec") }

    when:
    runner.runClass(derived)

    then:
    derived.log == ["ss1", "ss2", "s1", "s2", "c2", "c1", "cs2", "cs1"]
  }

  def "feature methods are run in correct order"() {
    def classes = compiler.compileWithImports("""
class BaseSpec extends Specification {
  static log = []

  def "feature 1"() {
    log << "b1"
    expect: true
  }

  def "feature 2"() {
    log << "b2"
    expect: true
  }
}

class DerivedSpec extends BaseSpec {
  def "feature 1"() {
    log << "d1"
    expect: true
  }

  def "feature 2"() {
    log << "d2"
    expect: true
  }
}
    """)

    def derived = classes.find { it.name.endsWith("DerivedSpec") }

    when:
    runner.runClass(derived)

    then:
    derived.log == ["b1", "b2", "d1", "d2"]
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
    fixtureMethod << ["setup", "cleanup", "setupSpeck", "cleanupSpeck"]
  }
}

