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

class StepwiseSpecsAndFeatures extends EmbeddedSpecification {
  def "spec annotation does not skip iterations on feature without annotation"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports("""
@Stepwise
class Foo extends Specification {
  def step1() {
    expect: count != 3
    where: count << (1..5)
  }
}
    """)

    then:
    // Ensure backward compatibility with pre-2.2 behaviour: If @Stepwise spec contains non-@Stepwise, data-driven
    // features, those features do not inherit any "skip rest after failure" logic on feature level. Users do have to
    // annotate individual features in order to activate skipping iterations after failure.
    result.testsStartedCount == 5 + 1
    result.testsSucceededCount == 4 + 1
    result.testsFailedCount == 1
    result.testsSkippedCount == 0
  }

  def "spec and feature annotations can be combined"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports("""
@Stepwise
class Foo extends Specification {
  def step1() { expect: true }
  @Stepwise
  def step2() {
    expect: count != 3
    where: count << (1..5)
  }
  def step3() { expect: true }
}
    """)

    then:
    result.testsStartedCount == 1 + 3 + 1
    result.testsSucceededCount == 1 + 2 + 1
    result.testsFailedCount == 1
    result.testsSkippedCount == 2 + 1
  }
}
