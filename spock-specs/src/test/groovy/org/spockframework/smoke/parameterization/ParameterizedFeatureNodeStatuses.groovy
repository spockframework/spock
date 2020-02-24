/*
 * Copyright 2020 the original author or authors.
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

package org.spockframework.smoke.parameterization

import org.opentest4j.MultipleFailuresError
import org.opentest4j.TestAbortedException
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockAssertionError
import org.spockframework.runtime.SpockComparisonFailure
import spock.lang.PendingFeature

class ParameterizedFeatureNodeStatuses extends EmbeddedSpecification {
  def setup() {
    runner.addClassImport(TestAbortedException)
    runner.throwFailure = false
  }

  @PendingFeature
  def 'should be skipped if all not-unrolled iterations are skipped'() {
    when:
    def result = runner.runSpecBody """
@Rollup
def 'a feature'() {
  throw new TestAbortedException()
  expect: a in [1, 2]
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      tests().executions().aborted().stream().forEach { it.terminationInfo.executionResult.throwable.get().printStackTrace() }

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 2
      totalFailureCount == 0
    }
  }

  @PendingFeature
  def 'should be successful if some not-unrolled iterations are successful and some skipped'() {
    when:
    def result = runner.runSpecBody """
@Rollup
def 'a feature'() {
  if (a == 1) throw new TestAbortedException()
  expect: a == 2
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 1
      testsFailedCount == 0

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 3
      totalFailureCount == 0
    }
  }

  @PendingFeature
  def 'should be failing if some not-unrolled iterations are failing and some skipped'() {
    when:
    def result = runner.runSpecBody """
@Rollup
def 'a feature'() {
  if (a == 1) throw new TestAbortedException()
  expect: a == 3
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [SpockComparisonFailure]

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @PendingFeature
  def 'should be error if some not-unrolled iterations are erroring and some skipped'() {
    when:
    def result = runner.runSpecBody """
@Rollup
def 'a feature'() {
  if (a == 1) throw new TestAbortedException()
  throw new Error()
  expect: true
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [Error]

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @PendingFeature
  def 'should be successful if all not-unrolled iterations are successful'() {
    when:
    def result = runner.runSpecBody """
@Rollup
def 'a feature'() {
  expect: a in [1, 2]
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 1
      testsFailedCount == 0

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 3
      totalFailureCount == 0
    }
  }

  @PendingFeature
  def 'should be failing if some not-unrolled iterations are failing and some successful'() {
    when:
    def result = runner.runSpecBody """
@Rollup
def 'a feature'() {
  expect: a == 1
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [SpockComparisonFailure]

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @PendingFeature
  def 'should be error if some not-unrolled iterations are erroring and some successful'() {
    when:
    def result = runner.runSpecBody """
@Rollup
def 'a feature'() {
  if (a == 1) throw new Error()
  expect: a == 2
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [Error]

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @PendingFeature
  def 'should be failing if all not-unrolled iterations are failing'() {
    when:
    def result = runner.runSpecBody """
@Rollup
def 'a feature'() {
  expect: a == 3
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      with(failures.exception) {
        it*.getClass() == [MultipleFailuresError]
        first().failures*.getClass() == [SpockComparisonFailure, SpockComparisonFailure]
      }

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @PendingFeature
  def 'should be failing if some not-unrolled iterations are erroring and some failing'() {
    when:
    def result = runner.runSpecBody """
@Rollup
def 'a feature'() {
  if (a == 1) throw new Error()
  expect: a == 3
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      with(failures.exception) {
        it*.getClass() == [MultipleFailuresError]
        first().failures*.getClass() == [Error, SpockComparisonFailure]
      }

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  @PendingFeature
  def 'should be failing if all not-unrolled iterations are erroring'() {
    when:
    def result = runner.runSpecBody """
@Rollup
def 'a feature'() {
  throw new Error()
  expect: a in [1, 2]
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      with(failures.exception) {
        it*.getClass() == [MultipleFailuresError]
        first().failures*.getClass() == [Error, Error]
      }

      dynamicallyRegisteredCount == 0

      containersStartedCount == 2
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 1

      totalStartedCount == 3
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 1
    }
  }

  def 'should be skipped if all unrolled iterations are skipped'() {
    when:
    def result = runner.runSpecBody """
@Unroll
def 'a feature'() {
  throw new TestAbortedException()
  expect: a in [1, 2]
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 1
      containersSucceededCount == 2
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 3
      testsSucceededCount == 0
      testsFailedCount == 0

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 3
      totalSucceededCount == 2
      totalFailureCount == 0
    }
  }

  def 'should be successful if some unrolled iterations are successful and some skipped'() {
    when:
    def result = runner.runSpecBody """
@Unroll
def 'a feature'() {
  if (a == 1) throw new TestAbortedException()
  expect: a == 2
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 2
      testsFailedCount == 0

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 4
      totalFailureCount == 0
    }
  }

  def 'should be failing if some unrolled iterations are failing and some skipped'() {
    when:
    def result = runner.runSpecBody """
@Unroll
def 'a feature'() {
  if (a == 1) throw new TestAbortedException()
  expect: a == 3
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [SpockComparisonFailure, SpockAssertionError]

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 1

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 2

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 2
      totalFailureCount == 2
    }
  }

  def 'should be failing if some unrolled iterations are erroring and some skipped'() {
    when:
    def result = runner.runSpecBody """
@Unroll
def 'a feature'() {
  if (a == 1) throw new TestAbortedException()
  throw new Error()
  expect: true
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [Error, SpockAssertionError]

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 1

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 2

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 2
      totalFailureCount == 2
    }
  }

  def 'should be successful if all unrolled iterations are successful'() {
    when:
    def result = runner.runSpecBody """
@Unroll
def 'a feature'() {
  expect: a in [1, 2]
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 3
      testsFailedCount == 0

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 5
      totalFailureCount == 0
    }
  }

  def 'should be failing if some unrolled iterations are failing and some successful'() {
    when:
    def result = runner.runSpecBody """
@Unroll
def 'a feature'() {
  expect: a == 1
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [SpockComparisonFailure, SpockAssertionError]

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 1

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 1
      testsFailedCount == 2

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 3
      totalFailureCount == 2
    }
  }

  def 'should be failing if some unrolled iterations are erroring and some successful'() {
    when:
    def result = runner.runSpecBody """
@Unroll
def 'a feature'() {
  if (a == 1) throw new Error()
  expect: a == 2
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [Error, SpockAssertionError]

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 1

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 1
      testsFailedCount == 2

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 3
      totalFailureCount == 2
    }
  }

  def 'should be failing if all unrolled iterations are failing'() {
    when:
    def result = runner.runSpecBody """
@Unroll
def 'a feature'() {
  expect: a == 3
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      with(failures.exception) {
        it*.getClass() == [SpockComparisonFailure, SpockComparisonFailure, SpockAssertionError]
        with(last().cause) {
          it.getClass() == MultipleFailuresError
          failures*.getClass() == [SpockComparisonFailure, SpockComparisonFailure]
        }
      }

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 1

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 3

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 3
    }
  }

  def 'should be failing if some unrolled iterations are erroring and some failing'() {
    when:
    def result = runner.runSpecBody """
@Unroll
def 'a feature'() {
  if (a == 1) throw new Error()
  expect: a == 3
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      with(failures.exception) {
        it*.getClass() == [Error, SpockComparisonFailure, SpockAssertionError]
        with(last().cause) {
          it.getClass() == MultipleFailuresError
          failures*.getClass() == [Error, SpockComparisonFailure]
        }
      }

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 1

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 3

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 3
    }
  }

  def 'should be failing if all unrolled iterations are erroring'() {
    when:
    def result = runner.runSpecBody """
@Unroll
def 'a feature'() {
  throw new Error()
  expect: a in [1, 2]
  where: a << [1, 2]
}
"""

    then:
    verifyAll(result) {
      with(failures.exception) {
        it*.getClass() == [Error, Error, SpockAssertionError]
        with(last().cause) {
          it.getClass() == MultipleFailuresError
          failures*.getClass() == [Error, Error]
        }
      }

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 2
      containersFailedCount == 1

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 3

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 2
      totalFailureCount == 3
    }
  }
}
