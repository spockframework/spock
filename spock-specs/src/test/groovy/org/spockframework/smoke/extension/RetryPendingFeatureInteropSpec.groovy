/*
 * Copyright 2025 the original author or authors.
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

import org.opentest4j.TestAbortedException
import org.spockframework.EmbeddedSpecification
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.PendingFeatureIf
import spock.lang.Retry
import spock.lang.Specification

/**
 * This specification shall test the interoperation between {@link PendingFeature} and the {@link Retry} extension.
 */
@Issue("https://github.com/spockframework/spock/issues/1863")
class RetryPendingFeatureInteropSpec extends EmbeddedSpecification {

  def setup() {
    runner.addClassImport(Specification)
    runner.addClassImport(PendingFeature)
    runner.addClassImport(PendingFeatureIf)
    runner.addClassImport(Retry)
    runner.addClassImport(TestAbortedException)
  }

  /**
   * This test and the next test checks for the different interceptor combinations, because PendingFeature and Retry
   * influence each other with the exception handling.
   */
  def "Retry feature shall be skipped when using PendingFeature"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @PendingFeature
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  def "feature"() {
    expect:
    false
  }
""")) {
      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
    }
  }

  /**
   * See test above, where the order of the annotations is switch, where the retry is the outer interceptor.
   */
  def "Retry feature inverse annotation order shall be skipped when using PendingFeature"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  @PendingFeature
  def "feature"() {
    expect:
    false
  }
""")) {
      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
    }
  }

  def "Retry feature shall be skipped when using PendingFeatureIf"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @PendingFeatureIf({ true })
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  def "Retry feature shall be skipped when using PendingFeatureIf"() {
    expect:
    false
  }
""")) {
      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
    }
  }

  def "Retry feature inverse annotation order shall be skipped when using PendingFeatureIf"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  @PendingFeatureIf({ true })
  def "feature"() {
    expect:
    false
  }
""")) {
      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
    }
  }

  def "Retry iteration shall be skipped when using PendingFeature"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @PendingFeature
  @Retry(mode = Retry.Mode.ITERATION)
  def "feature"() {
    expect:
    false
  }
""")) {
      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
    }
  }

  def "Retry feature parameterized shall be skipped when using PendingFeature"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @PendingFeature
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  def "feature"() {
    expect:
    if (i)
      throw new Exception()

    where:
    i << [1, 2]
  }
""")) {
      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 2
      testsSucceededCount == 1
      testsFailedCount == 0
    }
  }

  def "Retry iteration parameterized shall be skipped when using PendingFeature"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @PendingFeature
  @Retry(mode = Retry.Mode.ITERATION)
  def "feature"() {
    expect:
    if (i)
      throw new Exception()

    where:
    i << [1, 2]
  }
""")) {
      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 2
      testsSucceededCount == 1
      testsFailedCount == 0
    }
  }

  def "Retry iteration parameterized shall be skipped when using PendingFeatureIf"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @PendingFeatureIf({ true })
  @Retry(mode = Retry.Mode.ITERATION)
  def "feature"() {
    expect:
    if (i)
      throw new Exception()

    where:
    i << [1, 2]
  }
""")) {
      testsStartedCount == 3
      testsSkippedCount == 0
      testsAbortedCount == 2
      testsSucceededCount == 1
      testsFailedCount == 0
    }
  }
}
