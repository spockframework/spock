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

import org.junit.platform.engine.discovery.DiscoverySelectors

import org.spockframework.EmbeddedSpecification

class StepwiseExtension extends EmbeddedSpecification {
  def "[spec] basic usage"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports("""
@Stepwise
class Foo extends Specification {
  def step1() { expect: true }
  def step2() { expect: false }
  def step3() { expect: true }
}
    """)

    then:
    result.testsSucceededCount == 1
    result.testsStartedCount == 2
    result.testsFailedCount == 1
    result.testsSkippedCount == 1
  }

  def "[spec] automatically runs excluded methods that lead up to an included method"() {
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

  def "[spec] honors method-level @Ignore"() {
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

  def "[spec] sets childExecutionMode to SAME_THREAD"() {
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

  def "[feature] basic usage"() {
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

    then:
    result.testsStartedCount == 3 + 1
    result.testsSucceededCount == 2 + 1
    result.testsFailedCount == 1
    result.testsSkippedCount == 2
  }

  def "[feature] annotated non-data-driven features behave normally"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports("""
class Foo extends Specification {
  @Stepwise
  def step1() { expect: true }
  @Stepwise
  def step2() { expect: false }
  @Stepwise @Ignore
  def step3() { expect: false }
}
    """)

    then:
    result.testsStartedCount == 2
    result.testsSucceededCount == 1
    result.testsFailedCount == 1
    result.testsSkippedCount == 1
  }

  def "[feature] sets executionMode to SAME_THREAD"() {
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

  def "[mixed] spec annotation does not skip iterations on non-annotated feature"() {
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

  def "[mixed] annotations on both spec and feature work together"() {
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
