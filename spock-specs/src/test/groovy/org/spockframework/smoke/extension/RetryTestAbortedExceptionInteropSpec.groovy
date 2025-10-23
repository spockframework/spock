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
import spock.lang.IgnoreIf
import spock.lang.Issue
import spock.lang.Requires
import spock.lang.Retry
import spock.lang.Specification

/**
 * This specification shall test the interoperation of API using the {@link TestAbortedException} and the {@link Retry} extension.
 */
@Issue("https://github.com/spockframework/spock/issues/1863")
class RetryTestAbortedExceptionInteropSpec extends EmbeddedSpecification {

  def setup() {
    runner.addClassImport(Specification)
    runner.addClassImport(Requires)
    runner.addClassImport(IgnoreIf)
    runner.addClassImport(Retry)
    runner.addClassImport(TestAbortedException)
  }

  def "Retry iteration shall be skipped if a TestAbortedException is thrown"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @Retry(mode = Retry.Mode.ITERATION)
  def "feature"() {
    expect:
    throw new TestAbortedException()
  }
""")) {
      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
    }
  }

  def "Retry feature shall be skipped if a TestAbortedException is thrown"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  def "feature"() {
    expect:
    throw new TestAbortedException()
  }
  """)) {
      testsStartedCount == 1
      testsSkippedCount == 0
      testsAbortedCount == 1
      testsSucceededCount == 0
      testsFailedCount == 0
    }
  }

  def "Retry iteration parameterized shall be skipped if a TestAbortedException is thrown"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @Retry(mode = Retry.Mode.ITERATION)
  def "feature"() {
    expect:
    if (i > 0)
      throw new TestAbortedException(i.toString())

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

  def "Retry feature parameterized shall be skipped if a TestAbortedException is thrown"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  def "feature"() {
    expect:
    if (i > 0)
      throw new TestAbortedException(i.toString())

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

  def "Retry shall be skipped when feature is ignored"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @IgnoreIf({ true })
  @Retry
  def "feature"() {
    expect:
    false
  }
""")) {
      testsStartedCount == 0
      testsSkippedCount == 1
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 0
    }
  }

  def "Retry inverse shall be skipped when feature is ignored"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @Retry
  @IgnoreIf({ true })
  def "feature"() {
    expect:
    false
  }
""")) {
      testsStartedCount == 0
      testsSkippedCount == 1
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 0
    }
  }

  def "Retry parameterized shall be skipped when feature is ignored"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @IgnoreIf({ true })
  @Retry
  def "feature"() {
    expect:
    false

    where:
    i << [1, 2]
  }
""")) {
      testsStartedCount == 0
      testsSkippedCount == 1
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 0
    }
  }


  def "Retry inverse parameterized shall be skipped when feature is ignored"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @Retry
  @IgnoreIf({ true })
  def "feature"() {
    expect:
    false

    where:
    i << [1, 2]
  }
""")) {
      testsStartedCount == 0
      testsSkippedCount == 1
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 0
    }
  }

  def "Retry shall be skipped  when using Requires"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @Requires({ false })
  @Retry
  def "feature"() {
    expect:
    false
  }
""")) {
      testsStartedCount == 0
      testsSkippedCount == 1
      testsAbortedCount == 0
      testsSucceededCount == 0
      testsFailedCount == 0
    }
  }

  def "Retry iteration parameterized shall be skipped when using Requires"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @Requires({ data.get("i") && false })
  @Retry(mode = Retry.Mode.ITERATION)
  def "feature"() {
    expect:
    false

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

  def "Retry feature parameterized shall be skipped when using Requires"() {
    expect:
    verifyAll(runner.runSpecBody("""
  @Requires({ data.get("i") && false })
  @Retry(mode = Retry.Mode.SETUP_FEATURE_CLEANUP)
  def "feature"() {
    expect:
    false

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
