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

import org.spockframework.EmbeddedSpecification

class IncludeExcludeSpecs extends EmbeddedSpecification {
  List specs

  def setup() {
    compiler.addPackageImport(getClass().package)

    specs = compiler.compileWithImports("""
@Slow
class Spec1 extends Specification {
  def feature() {
    expect: true
  }
}

@Fast
class Spec2 extends Specification {
  def feature() {
    expect: true
  }
}

class Spec3 extends Specification {
  def feature() {
    expect: true
  }
}
class Spec4 extends Specification {
  @Fast
  def field = 1

  def feature() {
    expect: true
  }
}
class Spec5 extends Specification {
  def setup(@Fast def param) {}

  def feature() {
    expect: true
  }
}
  """)
  }

  def "include specs based on annotations"() {
    runner.configurationScript = {
      runner {
        include(*annotationTypes)
        scanParametersForIncludeExcludeAnnotationCriteria = scanParameters
        scanFieldsForIncludeExcludeAnnotationCriteria = scanFields
      }
    }

    when:
    def result = runner.runClasses(specs)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    where:
    annotationTypes | scanParameters | scanFields | runCount
    [Slow]          | false          | false      | 1
    [Fast]          | false          | false      | 1
    [Slow, Fast]    | false          | false      | 2

    [Slow]          | true           | false      | 1
    [Fast]          | true           | false      | 2
    [Slow, Fast]    | true           | false      | 3

    [Slow]          | false          | true       | 1
    [Fast]          | false          | true       | 2
    [Slow, Fast]    | false          | true       | 3

    [Slow]          | true           | true       | 1
    [Fast]          | true           | true       | 3
    [Slow, Fast]    | true           | true       | 4
  }

  def "exclude specs based on annotations"() {
    runner.configurationScript = {
      runner {
        exclude(*annotationTypes)
        scanParametersForIncludeExcludeAnnotationCriteria = scanParameters
        scanFieldsForIncludeExcludeAnnotationCriteria = scanFields
      }
    }

    when:
    def result = runner.runClasses(specs)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    where:
    annotationTypes | scanParameters | scanFields | runCount
    [Slow]          | false          | false      | 4
    [Fast]          | false          | false      | 4
    [Slow, Fast]    | false          | false      | 3

    [Slow]          | true           | false      | 4
    [Fast]          | true           | false      | 3
    [Slow, Fast]    | true           | false      | 2

    [Slow]          | false          | true       | 4
    [Fast]          | false          | true       | 3
    [Slow, Fast]    | false          | true       | 2

    [Slow]          | true           | true       | 4
    [Fast]          | true           | true       | 2
    [Slow, Fast]    | true           | true       | 1
  }

  def "include and exclude specs based on annotations"() {
    runner.configurationScript = {
      runner {
        include(*annInclude)
        exclude(*annExclude)
        scanParametersForIncludeExcludeAnnotationCriteria = scanParameters
        scanFieldsForIncludeExcludeAnnotationCriteria = scanFields
      }
    }

    when:
    def result = runner.runClasses(specs)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    where:
    annInclude   | annExclude   | scanParameters | scanFields | runCount
    [Slow]       | [Slow]       | false          | false      | 0
    [Slow]       | [Fast]       | false          | false      | 1
    [Slow]       | [Slow, Fast] | false          | false      | 0
    [Fast]       | [Slow]       | false          | false      | 1
    [Fast]       | [Fast]       | false          | false      | 0
    [Fast]       | [Slow, Fast] | false          | false      | 0
    [Slow, Fast] | [Slow]       | false          | false      | 1
    [Slow, Fast] | [Fast]       | false          | false      | 1
    [Slow, Fast] | [Slow, Fast] | false          | false      | 0

    [Slow]       | [Slow]       | true           | false      | 0
    [Slow]       | [Fast]       | true           | false      | 1
    [Slow]       | [Slow, Fast] | true           | false      | 0
    [Fast]       | [Slow]       | true           | false      | 2
    [Fast]       | [Fast]       | true           | false      | 0
    [Fast]       | [Slow, Fast] | true           | false      | 0
    [Slow, Fast] | [Slow]       | true           | false      | 2
    [Slow, Fast] | [Fast]       | true           | false      | 1
    [Slow, Fast] | [Slow, Fast] | true           | false      | 0

    [Slow]       | [Slow]       | false          | true       | 0
    [Slow]       | [Fast]       | false          | true       | 1
    [Slow]       | [Slow, Fast] | false          | true       | 0
    [Fast]       | [Slow]       | false          | true       | 2
    [Fast]       | [Fast]       | false          | true       | 0
    [Fast]       | [Slow, Fast] | false          | true       | 0
    [Slow, Fast] | [Slow]       | false          | true       | 2
    [Slow, Fast] | [Fast]       | false          | true       | 1
    [Slow, Fast] | [Slow, Fast] | false          | true       | 0

    [Slow]       | [Slow]       | true           | true       | 0
    [Slow]       | [Fast]       | true           | true       | 1
    [Slow]       | [Slow, Fast] | true           | true       | 0
    [Fast]       | [Slow]       | true           | true       | 3
    [Fast]       | [Fast]       | true           | true       | 0
    [Fast]       | [Slow, Fast] | true           | true       | 0
    [Slow, Fast] | [Slow]       | true           | true       | 3
    [Slow, Fast] | [Fast]       | true           | true       | 1
    [Slow, Fast] | [Slow, Fast] | true           | true       | 0
  }
}
