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

class IncludeExcludeFeatures extends EmbeddedSpecification {
  Class spec

  def setup() {
    compiler.addPackageImport(getClass().package)

    spec = compiler.compileSpecBody("""
@Slow
def feature1() {
  expect: true
}

@Fast
def feature2() {
  expect: true
}

def feature3() {
  expect: true
}

def feature4(@Fast def param) {
  expect: true
  where:
  param << [1]
}

def feature4(@Slow def param) {
  expect: true
  where:
  param << [1]
}
  """)
  }

  def "include methods based on annotations"() {
    runner.configurationScript = {
      runner {
        include(*annotationTypes)
        scanParametersForIncludeExcludeAnnotationCriteria = scanParameters
      }
    }

    when:
    def result = runner.runClass(spec)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    where:
    annotationTypes | scanParameters | runCount
    [Slow]          | false          | 1
    [Fast]          | false          | 1
    [Slow, Fast]    | false          | 2
    [Slow]          | true           | 3
    [Fast]          | true           | 3
    [Slow, Fast]    | true           | 6

  }

  def "include methods based on annotations using different configuration style"() {
    runner.configurationScript = {
      runner {
        include {
          annotationTypes.each {
            annotation(it)
          }
        }
      }
    }

    when:
    def result = runner.runClass(spec)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    where:
    annotationTypes | runCount
    [Slow]          | 1
    [Fast]          | 1
    [Slow, Fast]    | 2
  }

  def "exclude methods based on annotations"() {
    runner.configurationScript = {
      runner {
        exclude(*annotationTypes)
        scanParametersForIncludeExcludeAnnotationCriteria = scanParameters
      }
    }

    when:
    def result = runner.runClass(spec)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    where:
    annotationTypes | scanParameters | runCount
    [Slow]          | false          | 6
    [Fast]          | false          | 6
    [Slow, Fast]    | false          | 5
    [Slow]          | true           | 4
    [Fast]          | true           | 4
    [Slow, Fast]    | true           | 1
  }

  def "include and exclude features based on annotations"() {
    runner.configurationScript = {
      runner {
        include(*annInclude)
        exclude(*annExclude)
        scanParametersForIncludeExcludeAnnotationCriteria = scanParameters
      }
    }

    when:
    def result = runner.runClass(spec)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    where:
    annInclude   | annExclude   | scanParameters | runCount
    [Slow]       | [Slow]       | false          | 0
    [Slow]       | [Fast]       | false          | 1
    [Slow]       | [Slow, Fast] | false          | 0
    [Fast]       | [Slow]       | false          | 1
    [Fast]       | [Fast]       | false          | 0
    [Fast]       | [Slow, Fast] | false          | 0
    [Slow, Fast] | [Slow]       | false          | 1
    [Slow, Fast] | [Fast]       | false          | 1
    [Slow, Fast] | [Slow, Fast] | false          | 0

    [Slow]       | [Slow]       | true           | 0
    [Slow]       | [Fast]       | true           | 3
    [Slow]       | [Slow, Fast] | true           | 0
    [Fast]       | [Slow]       | true           | 3
    [Fast]       | [Fast]       | true           | 0
    [Fast]       | [Slow, Fast] | true           | 0
    [Slow, Fast] | [Slow]       | true           | 3
    [Slow, Fast] | [Fast]       | true           | 3
    [Slow, Fast] | [Slow, Fast] | true           | 0
  }
}
