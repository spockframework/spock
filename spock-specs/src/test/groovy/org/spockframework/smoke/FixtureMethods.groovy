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
import org.spockframework.compiler.InvalidSpecCompileException

import spock.lang.Issue

class FixtureMethods extends EmbeddedSpecification {
  public static ThreadLocal<List> log = new ThreadLocal<>()

  def setup() {
    log.set([])
  }

  def cleanup() {
    log.remove();
  }

  def "are run in correct order"() {
    when:
    runner.runSpecBody """
def getLog() { org.spockframework.smoke.FixtureMethods.log.get() }

def setup() { log << "s" }
def cleanup() { log << "c" }
def setupSpec() { log << "ss" }
def cleanupSpec() { log << "cs" }

def feature() { expect: true }
    """

    then:
    log.get() == ["ss", "s", "c", "cs"]
  }

  def "are run in correct order across class hierarchy"() {
    when:
    runner.runWithImports """
@Ignore
class Base extends Specification {
  def getLog() { org.spockframework.smoke.FixtureMethods.log.get() }

  def setup() { log << "s1" }
  def cleanup() { log << "c1" }
  def setupSpec() { log << "ss1" }
  def cleanupSpec() { log << "cs1" }
}

class Derived extends Base {
  def getLog() { org.spockframework.smoke.FixtureMethods.log.get() }

  def setup() { log << "s2" }
  def cleanup() { log << "c2" }
  def setupSpec() { log << "ss2" }
  def cleanupSpec() { log << "cs2" }

  def feature() { expect: true }
}
    """

    then:
    log.get() == ["ss1", "ss2", "s1", "s2", "c2", "c1", "cs2", "cs1"]
  }

  @Issue("http://issues.spockframework.org/detail?id=139")
  def "setupSpec() may not access instance fields (only @Shared and static fields)"() {
    when:
    compiler.compileSpecBody """
def x = 42

def setupSpec() {
  println x
}
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("@Shared")
  }

  @Issue("http://issues.spockframework.org/detail?id=139")
  def "cleanupSpec() may not access instance fields (only @Shared and static fields)"() {
    when:
    compiler.compileSpecBody """
def x = 42

def cleanupSpec() {
  3.times { x = 0 }
}
    """

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("@Shared")
  }

  def "cleanup() is run when setup() fails"() {
    runner.addClassMemberImport(FixtureMethods)

    when:
    runner.runSpecBody("""
def getLog() { org.spockframework.smoke.FixtureMethods.log.get() }
def setup() { throw new RuntimeException() }
def feature() { expect: true }
def cleanup() { log << "cleanup" }
    """)

    then:
    thrown(RuntimeException)
    log.get() == ["cleanup"]
  }

  def "cleanupSpec() is run when setupSpec() fails"() {
    runner.addClassMemberImport(FixtureMethods)

    when:
    runner.runSpecBody("""
def getLog() { org.spockframework.smoke.FixtureMethods.log.get() }
def setupSpec() { throw new RuntimeException() }
def feature() { expect: true }
def cleanupSpec() { log << "cleanupSpec" }
    """)

    then:
    thrown(RuntimeException)
    log.get() == ["cleanupSpec"]
  }

  def "cleanup() is not run when field initializer fails"() {
    runner.addClassImport(BlowUp)
    runner.addClassMemberImport(FixtureMethods)

    when:
    runner.runSpecBody("""
def x = new BlowUp()

def feature() { expect: true }
def cleanup() { log << "cleanup" }
    """)

    then:
    thrown(RuntimeException)
    log.get().empty
  }

  static class BlowUp {
    BlowUp() {
      throw new RuntimeException()
    }
  }
}
