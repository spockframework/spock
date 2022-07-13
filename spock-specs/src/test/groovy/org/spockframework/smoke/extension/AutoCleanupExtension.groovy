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

package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import spock.lang.Execution

import org.opentest4j.MultipleFailuresError

import static org.spockframework.runtime.model.parallel.ExecutionMode.SAME_THREAD

@Execution(SAME_THREAD)
class AutoCleanupExtension extends EmbeddedSpecification {
  static closable
  static disposable
  static boom1
  static boom2

  def setup() {
    runner.addClassImport(AutoCleanupExtension)
    closable = new MyClosable()
    disposable = new MyDisposable()
    boom1 = new Boom()
    boom2 = new Boom()
  }

  def "@AutoCleanup resources are cleaned up after cleanup()"() {
    assert !closable.called

    when:
    runner.runSpecBody("""
@AutoCleanup
closable = AutoCleanupExtension.closable

def feature() {
  expect: !closable.called
  cleanup: assert !closable.called
}

def cleanup() {
  assert !closable.called
}
    """)

    then:
    closable.called
  }

  def "@Shared @AutoCleanup resources are cleaned up after cleanupSpec()"() {
    assert !closable.called

    when:
    runner.runSpecBody("""
@Shared @AutoCleanup
closable = AutoCleanupExtension.closable

def feature() {
  expect: !closable.called
  cleanup: assert !closable.called
}

def cleanup() {
  assert !closable.called
}

def cleanupSpec() {
  assert !closable.called
}
    """)

    then:
    closable.called
  }

  def "may specify custom method to be called for cleanup"() {
    when:
    runner.runSpecBody("""
@AutoCleanup("dispose")
disposable = AutoCleanupExtension.disposable

def feature() {
  expect: true
}
    """)

    then:
    disposable.called
  }

  def "error during cleanup will fail feature"() {
    when:
    runner.runSpecBody("""
@AutoCleanup
boom = new AutoCleanupExtension.Boom()

def feature() {
  expect: true
}
    """)

    then:
    thrown(BoomException)
  }

  def "error during cleanup won't fail feature if 'quiet' option is used"() {
    when:
    runner.runSpecBody("""
@AutoCleanup(quiet = true)
boom = new AutoCleanupExtension.Boom()

def feature() {
  expect: true
}
    """)

    then:
    noExceptionThrown()
  }

  def "resources are cleaned up independently"() {
    runner.throwFailure = false

    when:
    def result = runner.runSpecBody("""
@AutoCleanup
boom1 = AutoCleanupExtension.boom1
@AutoCleanup
boom2 = AutoCleanupExtension.boom2

def feature() {
  expect: true
}
    """)

    then:
    boom1.called
    boom2.called

    and:
    result.failures.size() == 1
    with(result.failures[0].exception, MultipleFailuresError) {
      failures[0] instanceof BoomException
      failures[1] instanceof BoomException
    }
  }

  // debatable
  def "no cleanup if field initialization fails"() {
    assert !closable.called

    when:
    runner.runSpecBody("""
@AutoCleanup
closable = AutoCleanupExtension.closable
def x = new AutoCleanupExtension.BlowUp()

def feature() { expect: true }
    """)

    then:
    thrown(BoomException)
    !closable.called
  }

  // debatable
  def "no cleanup if shared field initialization fails"() {
    assert !closable.called

    when:
    runner.runSpecBody("""
@Shared @AutoCleanup
closable = AutoCleanupExtension.closable
@Shared
def x = new AutoCleanupExtension.BlowUp()

def feature() { expect: true }
    """)

    then:
    thrown(BoomException)
    !closable.called
  }

  static class MyClosable {
    def called = false

    def close() { called = true }
  }

  static class MyDisposable {
    def called = false

    def dispose() { called = true }
  }

  static class Boom {
    def called = false

    def close() { called = true; throw new BoomException() }
  }

  static class BlowUp {
    BlowUp() {
      throw new BoomException()
    }
  }

  static class BoomException extends Exception {}
}



