/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification

class AutoCleanupExtension extends EmbeddedSpecification {
  public static ThreadLocal<MyClosable> closable = new ThreadLocal<>();
  public static ThreadLocal<MyDisposable> disposable = new ThreadLocal<>();
  public static ThreadLocal<Boom> boom1 = new ThreadLocal<>();
  public static ThreadLocal<Boom> boom2 = new ThreadLocal<>();

  def setup() {
    runner.addClassImport(AutoCleanupExtension)
    closable.set(new MyClosable());
    disposable.set(new MyDisposable());
    boom1.set(new Boom());
    boom2.set(new Boom());
  }

  void cleanup() {
    closable.remove()
    disposable.remove()
    boom1.remove()
    boom2.remove()
  }

  def "@AutoCleanup resources are cleaned up after cleanup()"() {
    assert !closable.get().called

    when:
    runner.runSpecBody("""
@AutoCleanup
closable = AutoCleanupExtension.closable.get()

def feature() {
  expect: !closable.called
  cleanup: assert !closable.called
}

def cleanup() {
  assert !closable.called
}
    """)

    then:
    closable.get().called
  }

  def "@Shared @AutoCleanup resources are cleaned up after cleanupSpec()"() {
    assert !closable.get().called

    when:
    runner.runSpecBody("""
@Shared @AutoCleanup
closable = AutoCleanupExtension.closable.get()

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
    closable.get().called
  }

  def "may specify custom method to be called for cleanup"() {
    when:
    runner.runSpecBody("""
@AutoCleanup("dispose")
disposable = AutoCleanupExtension.disposable.get()

def feature() {
  expect: true
}
    """)

    then:
    disposable.get().called
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
boom1 = AutoCleanupExtension.boom1.get()
@AutoCleanup
boom2 = AutoCleanupExtension.boom2.get()

def feature() {
  expect: true
}
    """)

    then:
    boom1.get().called
    boom2.get().called

    and:
    result.failures.size() == 2
    result.failures[0].exception instanceof BoomException
    result.failures[1].exception instanceof BoomException
  }

  // debatable
  def "no cleanup if field initialization fails"() {
    assert !closable.get().called

    when:
    runner.runSpecBody("""
@AutoCleanup
closable = AutoCleanupExtension.closable.get()
def x = new AutoCleanupExtension.BlowUp()

def feature() { expect: true }
    """)

    then:
    thrown(BoomException)
    !closable.get().called
  }

  // debatable
  def "no cleanup if shared field initialization fails"() {
    assert !closable.get().called

    when:
    runner.runSpecBody("""
@Shared @AutoCleanup
closable = AutoCleanupExtension.closable.get()
@Shared
def x = new AutoCleanupExtension.BlowUp()

def feature() { expect: true }
    """)

    then:
    thrown(BoomException)
    !closable.get().called
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



