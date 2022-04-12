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
    result.testsStartedCount == 0
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsAbortedCount == 0
    result.testsSucceededCount == 0
    result.containersSkippedCount == 1
    result.containersFailedCount == 0

    where:
    [specAnnotation, featureAnnotation] << [
      ["@Requires({ false })", "@IgnoreIf({ true })", "@Ignore"],
      ["@Requires({ undeclaredVariable })", "@IgnoreIf({ undeclaredVariable })", "@PendingFeatureIf({ undeclaredVariable })"]
    ].combinations()

    // Used in @Unroll
    specAnnotationName = specAnnotation.split("\\(")[0]
    featureAnnotationName = featureAnnotation.split("\\(")[0]
  }

  @Unroll("#specAnnotationName #skipsSpec, #featureAnnotationName on feature #isEvaluated")
  @Issue("https://github.com/spockframework/spock/issues/1458")
  def "feature skipping condition is not evaluated in skipped spec with inheritance"() {
    runner.throwFailure = false

    when:
    def result = runner.runWithImports """
${specAnnotation}inherited = $inherited)
abstract class Foo extends Specification {
  def "foo feature"() {
    expect: true
  }
}

abstract class Bar extends Foo {
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
    result.testsStartedCount == 0
    result.testsFailedCount == 0
    result.testsSkippedCount == 0
    result.testsAbortedCount == 0
    result.testsSucceededCount == 0
    result.containersSkippedCount == containersSkippedCount
    result.containersFailedCount == containersFailedCount

    where:
    [specAnnotation, inherited, featureAnnotation] << [
      ["@Requires(value = { false }, ", "@IgnoreIf(value = { true }, ", "@Ignore("],
      [true, false],
      ["@Requires({ undeclaredVariable })", "@IgnoreIf({ undeclaredVariable })", "@PendingFeatureIf({ undeclaredVariable })"]
    ].combinations()
    containersSkippedCount = inherited ? 1 : 0
    containersFailedCount = inherited ? 0 : 1

    // Used in @Unroll
    specAnnotationName = specAnnotation.split("\\(")[0]
    featureAnnotationName = featureAnnotation.split("\\(")[0]
    skipsSpec = inherited ? "with inheritance skips spec" : "without inheritance does not skip spec"
    isEvaluated = inherited ? "not evaluated" : "evaluated"
  }

}
