/*
 * Copyright 2026 the original author or authors.
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

package org.spockframework.smoke.mock

import org.spockframework.runtime.ConditionNotSatisfiedError
import spock.lang.FailsWith
import spock.lang.Issue
import spock.lang.Specification

import java.util.function.Function

abstract class GlobalInteractionsInCleanupSuperSpec extends Specification {
  def cleanup() {
    verifyAll {
      mock.apply("a") == "b"
      mock2.apply("a") == "c"
      mock3.apply("a") == "d"
    }
  }
}

@Issue("https://github.com/spockframework/spock/issues/616")
class GlobalInteractionsInCleanup extends GlobalInteractionsInCleanupSuperSpec {
  def mock = Mock(Function)
  def mock2 = Mock(Function) {
    apply("a") >> { "c" }
  }
  def mock3 = Mock(Function)

  def setup() {
    mock.apply("a") >> { "b" }
  }

  def cleanup() {
    verifyAll {
      mock.apply("a") == "b"
      mock2.apply("a") == "c"
      mock3.apply("a") == "d"
    }
  }

  def "run a successful test"() {
    given:
    mock3.apply("a") >> { "d" }

    expect:
    true
  }

  @FailsWith(ConditionNotSatisfiedError)
  def "run a failing test"() {
    given:
    mock3.apply("a") >> { "d" }

    expect:
    false
  }
}
