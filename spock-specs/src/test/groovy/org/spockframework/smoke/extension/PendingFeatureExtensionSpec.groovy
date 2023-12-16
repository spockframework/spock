/*
 * Copyright 2023 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.smoke.extension

import org.assertj.core.api.Condition
import org.opentest4j.MultipleFailuresError
import org.opentest4j.TestAbortedException
import org.spockframework.EmbeddedSpecification

import static org.junit.platform.testkit.engine.EventConditions.abortedWithReason
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.cause
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message

class PendingFeatureExtensionSpec extends EmbeddedSpecification {
  def setup() {
    runner.throwFailure = false
    runner.addClassImport(TestAbortedException)
  }

  def "@PendingFeature marks failing feature as skipped"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
def bar() {
  expect: false
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 1
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('Feature not yet implemented correctly.'))
      )
    }
  }

  def "@PendingFeature includes reason in exception message"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature(reason='42')
def bar() {
  expect: false
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 1
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('Feature not yet implemented correctly. Reason: 42'))
      )
    }
  }

  def "@PendingFeature marks feature that fails with exception as skipped"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
def bar() {
  expect:
  throw new Exception()
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 1
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('Feature not yet implemented correctly.'))
      )
    }
  }

  def "@PendingFeature rethrows non handled exceptions"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature(exceptions=[IndexOutOfBoundsException])
def bar() {
  expect:
  throw new IllegalArgumentException()
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(instanceOf(IllegalArgumentException))
      )
    }
  }

  def "@PendingFeature marks passing feature as failed"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
def bar() {
  expect: true
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(message('Feature is marked with @PendingFeature but passes unexpectedly'))
      )
    }
  }

  def "@PendingFeature ignores aborted feature"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
def bar() {
  throw new TestAbortedException('ignored because I can')
  expect: true
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 1
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('ignored because I can'))
      )
    }
  }

  def "@PendingFeature marks uprolled data driven feature where every iteration fails as skipped"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
@Rollup
def bar() {
  expect: test

  where:
  test << [false, false, false]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 1
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('Feature not yet implemented correctly.'))
      )
    }
  }

  def "@PendingFeature marks unrolled data driven feature iterations that fail as skipped"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
def bar() {
  expect: test

  where:
  test << [false, false, false]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 4
      testsSucceededCount == 1
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 3
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('Feature not yet implemented correctly.')),
        abortedWithReason(message('Feature not yet implemented correctly.')),
        abortedWithReason(message('Feature not yet implemented correctly.')),
      )
    }
  }

  def "@PendingFeature marks uprolled data driven feature where one iteration fails others are successful as skipped"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
@Rollup
def bar() {
  expect: test

  where:
  test << [true, false, true]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 1
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('Feature not yet implemented correctly.'))
      )
    }
  }

  def "@PendingFeature marks unrolled data driven feature where one iteration fails others are successful as skipped"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
def bar() {
  expect: test

  where:
  test << [true, false, true]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 4
      testsSucceededCount == 3
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 1
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('Feature not yet implemented correctly.'))
      )
    }
  }

  def "@PendingFeature marks uprolled data driven feature where all iterations pass as failed"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
@Rollup
def bar() {
  expect: test

  where:
  test << [true, true, true]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(message('Feature is marked with @PendingFeature but passes unexpectedly'))
      )
    }
  }

  def "@PendingFeature marks unrolled data driven feature where all iterations pass as failed"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
def bar() {
  expect: test

  where:
  test << [true, true, true]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 4
      testsSucceededCount == 3
      testsFailedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(message('Feature is marked with @PendingFeature but passes unexpectedly'))
      )
    }
  }

  def "@PendingFeature rethrows non handled exceptions in uprolled data driven feature"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature(exceptions=[IndexOutOfBoundsException])
@Rollup
def bar() {
  expect:
  throw new IllegalArgumentException()

  where:
  test << [false, false, false]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(
          instanceOf(MultipleFailuresError),
          new Condition<>({ it.failures*.getClass() == [IllegalArgumentException] * 3 }, 'failures match')
        )
      )
    }
  }

  def "@PendingFeature does not rethrow non handled exceptions in unrolled data driven feature"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature(exceptions=[IndexOutOfBoundsException])
def bar() {
  expect:
  throw new IllegalArgumentException()

  where:
  test << [false, false, false]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 4
      testsSucceededCount == 1
      testsFailedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(instanceOf(IllegalArgumentException)),
        finishedWithFailure(instanceOf(IllegalArgumentException)),
        finishedWithFailure(instanceOf(IllegalArgumentException)),
      )
    }
  }

  def "@PendingFeature rethrows non handled exceptions in uprolled data driven feature even if some iterations are failing"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature(exceptions=[IndexOutOfBoundsException])
@Rollup
def bar() {
  expect:
  if (test) throw new IllegalArgumentException()
  throw new IndexOutOfBoundsException()

  where:
  test << [false, true, false]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(instanceOf(IllegalArgumentException))
      )
    }
  }

  def "@PendingFeature does not rethrow non handled exceptions in unrolled data driven feature even if some iterations are failing"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature(exceptions=[IndexOutOfBoundsException])
def bar() {
  expect:
  if (test) throw new IllegalArgumentException()
  throw new IndexOutOfBoundsException()

  where:
  test << [false, true, false]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 4
      testsSucceededCount == 1
      testsFailedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 2
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('Feature not yet implemented correctly.')),
        abortedWithReason(message('Feature not yet implemented correctly.'))
      )
    }
  }

  def "@PendingFeature rethrows non handled exceptions in uprolled data driven feature even if some iterations are passing"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature(exceptions=[IndexOutOfBoundsException])
@Rollup
def bar() {
  expect:
  if (!test) throw new IllegalArgumentException()
  test

  where:
  test << [false, true, false]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(
          instanceOf(MultipleFailuresError),
          new Condition<>({ it.failures*.getClass() == [IllegalArgumentException] * 2 }, 'failures match')
        )
      )
    }
  }

  def "@PendingFeature does not rethrow non handled exceptions in unrolled data driven feature even if some iterations are passing"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature(exceptions=[IndexOutOfBoundsException])
def bar() {
  expect:
  if (!test) throw new IllegalArgumentException()
  test

  where:
  test << [false, true, false]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 4
      testsSucceededCount == 2
      testsFailedCount == 2
      testsSkippedCount == 0
      testsAbortedCount == 0
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(instanceOf(IllegalArgumentException)),
        finishedWithFailure(instanceOf(IllegalArgumentException)),
      )
    }
  }

  def "@PendingFeature ignores uprolled data driven feature where all iterations are aborted"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
@Rollup
def bar() {
  throw new TestAbortedException('ignored because I can')
  expect: test

  where:
  test << [true, true, true]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 1
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(
          message('All iterations were aborted'),
          cause(
            instanceOf(MultipleFailuresError),
            new Condition<>({ it.failures*.message == ['ignored because I can'] * 3 }, 'failures match')
          )
        )
      )
    }
  }

  def "@PendingFeature ignores unrolled data driven feature where all iterations are aborted"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
def bar() {
  throw new TestAbortedException('ignored because I can')
  expect: test

  where:
  test << [true, true, true]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 4
      testsSucceededCount == 1
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 3
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('ignored because I can')),
        abortedWithReason(message('ignored because I can')),
        abortedWithReason(message('ignored because I can')),
      )
    }
  }

  def "@PendingFeature marks uprolled data driven feature where some iterations are aborted and some pass as failed"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
@Rollup
def bar() {
  if (!test) throw new TestAbortedException('ignored because I can')
  expect: test

  where:
  test << [true, false, true]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(message('Feature is marked with @PendingFeature but passes unexpectedly'))
      )
    }
  }

  def "@PendingFeature marks unrolled data driven feature where some iterations are aborted and some pass as failed"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
def bar() {
  if (!test) throw new TestAbortedException('ignored because I can')
  expect: test

  where:
  test << [true, false, true]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 4
      testsSucceededCount == 2
      testsFailedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 1
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('ignored because I can'))
      )
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(message('Feature is marked with @PendingFeature but passes unexpectedly'))
      )
    }
  }

  def "@PendingFeature marks uprolled data driven feature where some iterations are aborted and some fail as skipped"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
@Rollup
def bar() {
  if (test) throw new TestAbortedException('ignored because I can')
  expect: test

  where:
  test << [true, false, true]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 1
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('Feature not yet implemented correctly.'))
      )
    }
  }

  def "@PendingFeature does not mark unrolled data driven feature where some iterations are aborted and some fail as skipped"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature
def bar() {
  if (test) throw new TestAbortedException('ignored because I can')
  expect: test

  where:
  test << [true, false, true]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 4
      testsSucceededCount == 1
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 3
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('ignored because I can')),
        abortedWithReason(message('Feature not yet implemented correctly.')),
        abortedWithReason(message('ignored because I can')),
      )
    }
  }

  def "@PendingFeature rethrows non handled exceptions in uprolled data driven feature even if some iterations are aborted"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature(exceptions=[IndexOutOfBoundsException])
@Rollup
def bar() {
  expect:
  if (test) throw new IllegalArgumentException()
  throw new TestAbortedException('ignored because I can')

  where:
  test << [false, true, false]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(instanceOf(IllegalArgumentException))
      )
    }
  }

  def "@PendingFeature does not rethrow non handled exceptions in unrolled data driven feature even if some iterations are aborted"() {
    when:
    def result = runner.runSpecBody """
@PendingFeature(exceptions=[IndexOutOfBoundsException])
def bar() {
  expect:
  if (test) throw new IllegalArgumentException()
  throw new TestAbortedException('ignored because I can')

  where:
  test << [false, true, false]
}
"""

    then:
    verifyAll(result) {
      testsStartedCount == 4
      testsSucceededCount == 1
      testsFailedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 2
      testEvents().aborted().assertEventsMatchExactly(
        abortedWithReason(message('ignored because I can')),
        abortedWithReason(message('ignored because I can'))
      )
      testEvents().failed().assertEventsMatchExactly(
        finishedWithFailure(instanceOf(IllegalArgumentException)),
      )
    }
  }
}
