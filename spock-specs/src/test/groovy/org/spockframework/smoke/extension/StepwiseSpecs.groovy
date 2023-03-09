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

import org.junit.platform.engine.FilterResult
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.PostDiscoveryFilter
import org.spockframework.EmbeddedSpecification
import spock.lang.Issue
import spock.lang.PendingFeature

class StepwiseSpecs extends EmbeddedSpecification {
  def "basic usage"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports("""
@Stepwise
class Foo extends Specification {
  def step1() { expect: true }
  def step2() { expect: false }
  def step3() { expect: true }
  def step4() { expect: true }
}
    """)
    def expectedSkipMessagesCount = result.testEvents()
      .filter({ event ->
        event.payload.present &&
          event.payload.get() == "Skipped due to previous Error (by @Stepwise)"
      })
      .count()

    then:
    result.testsSucceededCount == 1
    result.testsStartedCount == 2
    result.testsFailedCount == 1
    result.testsSkippedCount == 2
    expectedSkipMessagesCount == 2
  }

  def "automatically runs excluded methods that lead up to an included method"() {
    def clazz = compiler.compileWithImports("""
@Stepwise
class Foo extends Specification {
  def step1() { expect: true }
  def step2() { expect: true }
  def step3() { expect: true }
}
    """)[0]

    when:
    def result = runner.runWithSelectors(DiscoverySelectors.selectMethod(clazz, "step3"))

    then:
    result.testsSucceededCount == 3
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }

  @PendingFeature
  @Issue("https://github.com/spockframework/spock/issues/1593")
  def "automatically runs excluded methods that lead up to an included method with post discovery filter"() {
    def clazz = compiler.compileWithImports("""
@Stepwise
class Foo extends Specification {
  def step1() { expect: true }
  def step2() { expect: true }
  def step3() { expect: true }
}
    """)[0]

    when:
    def result = runner.runClass(clazz, {
      FilterResult.includedIf(it.displayName == 'step3')
    } as PostDiscoveryFilter)

    then:
    result.testsSucceededCount == 3
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }

  def "honors method-level @Ignore"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports("""
@Stepwise
class Foo extends Specification {
  def step1() { expect: true }
  @Ignore
  def step2() { expect: true }
  def step3() { expect: true }
}
    """)

    then:
    result.testsSucceededCount == 2
    result.testsFailedCount == 0
    result.testsSkippedCount == 1
  }

  def "sets childExecutionMode to SAME_THREAD"() {
    when:
    def result = runner.runWithImports("""
import org.spockframework.runtime.model.parallel.ExecutionMode
@Stepwise
class Foo extends Specification {
  def step1() { expect: specificationContext.currentSpec.childExecutionMode.get() == ExecutionMode.SAME_THREAD }
}
    """)

    then:
    result.testsSucceededCount == 1
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
  }
}
