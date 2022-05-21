/*
 * Copyright 2022 the original author or authors.
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

package org.spockframework.smoke.extension

import org.spockframework.EmbeddedSpecification
import spock.lang.*

class MixedSpecSkippingExtensions extends EmbeddedSpecification {

  @Unroll("#specAnnotationName skips spec, #featureAnnotationName on feature not evaluated")
  @Issue("https://github.com/spockframework/spock/issues/1458")
  def "feature skipping condition is not evaluated in skipped spec"() {
    when:
    def result = runner.runWithImports """
$specAnnotation
class Foo extends Specification {
  $featureAnnotation
  def "basic usage"() {
    expect: true
  }
}
"""

    then:
    with(result) {
      testsStartedCount == 0
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 0
      containersSkippedCount == 1
      containersFailedCount == 0
    }

    where:
    [specAnnotation, featureAnnotation] << [
      ["@Requires({ false })", "@IgnoreIf({ true })", "@Ignore"],
      ["@Requires({ undeclaredVariable })", "@IgnoreIf({ undeclaredVariable })", "@PendingFeatureIf({ undeclaredVariable })"]
    ].combinations()

    // Used in @Unroll
    specAnnotationName = specAnnotation.split(/\(/)[0]
    featureAnnotationName = featureAnnotation.split(/\(/)[0]
  }

  @Unroll("#specAnnotationName #skipsSpec, #featureAnnotationName on feature #isEvaluated")
  @Issue("https://github.com/spockframework/spock/issues/1458")
  def "feature skipping condition is not evaluated in skipped spec with inheritance"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports """
class Base extends Specification {
  def "base feature"() {
    expect: true
  }
}

${specAnnotation}inherited = $inherited)
class Foo extends Base {
  $featureAnnotation
  def "foo feature"() {
    expect: true
  }
}

class Bar extends Foo {
  $featureAnnotation
  def "bar feature"() {
    expect: true
  }
}

class Test extends Bar {
  $featureAnnotation
  def "test feature"() {
    expect: true
  }
}
"""

    then:
    with(result) {
      testsStartedCount == 1  // Base
      testsFailedCount == 0
      testsSkippedCount == 0
      testsAbortedCount == 0
      testsSucceededCount == 1  // Base
      containersSkippedCount == containersSkippedCount
      containersFailedCount == containersFailedCount
    }

    where:
    [specAnnotation, inherited, featureAnnotation] << [
      ["@Requires(value = { false }, ", "@IgnoreIf(value = { true }, ", "@Ignore("],
      [true, false],
      ["@Requires({ undeclaredVariable })", "@IgnoreIf({ undeclaredVariable })", "@PendingFeatureIf({ undeclaredVariable })"]
    ].combinations()

    containersSkippedCount = inherited
      ? 3  // Foo + child classes Bar, Test
      : 1  // Foo (no child classes)

    containersFailedCount = inherited
      ? 0  // all fault conditions skipped
      : 2  // faulty conditions in Bar, Test not skipped

    // Used in @Unroll
    specAnnotationName = specAnnotation.split(/\(/)[0]
    featureAnnotationName = featureAnnotation.split(/\(/)[0]
    skipsSpec = inherited ? "with inheritance skips spec" : "without inheritance does not skip spec"
    isEvaluated = inherited ? "not evaluated" : "evaluated"
  }

}
