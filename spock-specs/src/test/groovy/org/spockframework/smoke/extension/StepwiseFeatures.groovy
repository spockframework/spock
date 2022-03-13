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
import org.spockframework.runtime.InvalidSpecException

class StepwiseFeatures extends EmbeddedSpecification {
  def "basic usage"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports("""
class Foo extends Specification {
  @Stepwise
  def step1() {
    expect: count != 3
    where: count << (1..5)
  }
}
    """)
    def expectedSkipMessagesCount = result.testEvents()
      .filter({ event ->
        event.payload.present &&
          event.payload.get() == "skipping subsequent iterations after failure"
      })
      .count()

    then:
    result.testsStartedCount == 3 + 1
    result.testsSucceededCount == 2 + 1
    result.testsFailedCount == 1
    result.testsSkippedCount == 2
    expectedSkipMessagesCount == 2
  }

  def "annotated feature must be data-driven"() {
    when:
    def result = runner.runWithImports("""
class Foo extends Specification {
  @Stepwise
  def step1() { expect: true }
}
    """)

    then:
    thrown(InvalidSpecException)
  }

  def "feature annotation sets executionMode to SAME_THREAD"() {
    when:
    def result = runner.runWithImports("""
import org.spockframework.runtime.model.parallel.ExecutionMode
class Foo extends Specification {
  @Stepwise
  def step1() {
    expect: specificationContext.currentFeature.executionMode.get() == ExecutionMode.SAME_THREAD
    where: count << (1..2)
  }
}
    """)

    then:
    result.testsStartedCount == 2 + 1
    result.testsSucceededCount == 2 + 1
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }
}
