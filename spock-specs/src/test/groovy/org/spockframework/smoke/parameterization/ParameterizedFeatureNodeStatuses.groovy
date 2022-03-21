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

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockComparisonFailure
import spock.lang.Issue

import org.opentest4j.*

class ParameterizedFeatureNodeStatuses extends EmbeddedSpecification {
  def setup() {
    runner.addClassImport(TestAbortedException)
    runner.throwFailure = false
  }

  def 'should be skipped if all uprolled iterations are skipped'() {
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

  def 'should be skipped if there is one uprolled iteration and it is skipped'() {
    when:
    def result = runner.runSpecBody """
@Rollup
def 'a feature'() {
  throw new TestAbortedException()
  expect: a in [1, 2]
  where: a = 1
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

  def 'should be successful if some uprolled iterations are successful and some skipped'() {
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

  def 'should be failing if some uprolled iterations are failing and some skipped'() {
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

  def 'should be failing if there is one uprolled iteration and it is failing'() {
    when:
    def result = runner.runSpecBody """
@Rollup
def 'a feature'() {
  expect: a == 3
  where: a = 1
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

  def 'should be error if some uprolled iterations are erroring and some skipped'() {
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

  def 'should be successful if all uprolled iterations are successful'() {
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

  def 'should be failing if some uprolled iterations are failing and some successful'() {
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

  def 'should be error if some uprolled iterations are erroring and some successful'() {
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

  def 'should be failing if all uprolled iterations are failing'() {
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

  def 'should be failing if some uprolled iterations are erroring and some failing'() {
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

  def 'should be failing if all uprolled iterations are erroring'() {
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

  def 'should be successful if all unrolled iterations are skipped'() {
    when:
    def result = runner.runFeatureBody """
throw new TestAbortedException()
expect: a in [1, 2]
where: a << [1, 2]
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
      testsAbortedCount == 2
      testsSucceededCount == 1
      testsFailedCount == 0

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 2
      totalSucceededCount == 3
      totalFailureCount == 0
    }
  }

  def 'should be successful if some unrolled iterations are successful and some skipped'() {
    when:
    def result = runner.runFeatureBody """
if (a == 1) throw new TestAbortedException()
expect: a == 2
where: a << [1, 2]
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

  def 'should be successful if some unrolled iterations are failing and some skipped'() {
    when:
    def result = runner.runFeatureBody """
if (a == 1) throw new TestAbortedException()
expect: a == 3
where: a << [1, 2]
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [SpockComparisonFailure]

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 1
      testsFailedCount == 1

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 3
      totalFailureCount == 1
    }
  }

  def 'should be successful if some unrolled iterations are erroring and some skipped'() {
    when:
    def result = runner.runFeatureBody """
if (a == 1) throw new TestAbortedException()
throw new Error()
expect: true
where: a << [1, 2]
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [Error]

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 1
      testsFailedCount == 1

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 3
      totalFailureCount == 1
    }
  }

  def 'should be successful if all unrolled iterations are successful'() {
    when:
    def result = runner.runFeatureBody """
expect: a in [1, 2]
where: a << [1, 2]
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

  def 'should be successful if some unrolled iterations are failing and some successful'() {
    when:
    def result = runner.runFeatureBody """
expect: a == 1
where: a << [1, 2]
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [SpockComparisonFailure]

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 2
      testsFailedCount == 1

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 4
      totalFailureCount == 1
    }
  }

  def 'should be successful if some unrolled iterations are erroring and some successful'() {
    when:
    def result = runner.runFeatureBody """
if (a == 1) throw new Error()
expect: a == 2
where: a << [1, 2]
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [Error]

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 2
      testsFailedCount == 1

      totalStartedCount == 5
      totalSkippedCount == 0
      totalAbortedCount == 0
      totalSucceededCount == 4
      totalFailureCount == 1
    }
  }

  def 'should be successful if all unrolled iterations are failing'() {
    when:
    def result = runner.runFeatureBody """
expect: a == 3
where: a << [1, 2]
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [SpockComparisonFailure, SpockComparisonFailure]

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

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

  def 'should be successful if some unrolled iterations are erroring and some failing'() {
    when:
    def result = runner.runFeatureBody """
if (a == 1) throw new Error()
expect: a == 3
where: a << [1, 2]
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [Error, SpockComparisonFailure]

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

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

  def 'should be successful if all unrolled iterations are erroring'() {
    when:
    def result = runner.runFeatureBody """
throw new Error()
expect: a in [1, 2]
where: a << [1, 2]
"""

    then:
    verifyAll(result) {
      failures.exception*.getClass() == [Error, Error]

      dynamicallyRegisteredCount == 2

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

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

  @Issue("https://github.com/spockframework/spock/issues/1441")
  def 'exceptions in iteration interceptors should not affect other iterations'() {
    when:
    def result = runner.runWithImports """
import org.opentest4j.TestAbortedException
import org.spockframework.runtime.AbstractRunListener
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.parallel.ExecutionMode
import org.spockframework.util.SpockReleaseInfo
import spock.lang.Specification

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

class StepwiseIterationsTest extends Specification {

  @StepwiseIterations
  def "myFeature #count"() {
    expect:
    1 == 1

    where:
    count << (1..5)
  }
}

// ------------------------------------------------------------------------

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ExtensionAnnotation(StepwiseIterationsExtension)
@interface StepwiseIterations {}

// ------------------------------------------------------------------------

class StepwiseIterationsExtension implements IAnnotationDrivenExtension<StepwiseIterations> {
  @Override
  void visitFeatureAnnotation(StepwiseIterations annotation, FeatureInfo feature) {
    feature.addIterationInterceptor(new ThrowingInterceptor())
  }


  static class ThrowingInterceptor implements IMethodInterceptor {

    @Override
    void intercept(IMethodInvocation invocation) throws Throwable {
      if (invocation.iteration.iterationIndex == 2) {
        throw new TestAbortedException("Test aborted")
      }
      invocation.proceed()
    }
  }
}
"""

    then:
    verifyAll(result) {

      dynamicallyRegisteredCount == 5

      containersStartedCount == 3
      containersSkippedCount == 0
      containersAbortedCount == 0
      containersSucceededCount == 3
      containersFailedCount == 0

      testsStartedCount == 6
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 5
      testsFailedCount == 0

      totalStartedCount == 8
      totalSkippedCount == 0
      totalAbortedCount == 1
      totalSucceededCount == 7
      totalFailureCount == 0
    }
  }
}
