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

class IncludeExcludeFeatures extends EmbeddedSpecification {
  Class spec

  def setup() {
    compiler.addImport(getClass().package)

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
  """)
  }

  def "include methods based on annotations"() {
    runner.configurationScript = {
      runner {
        include(*annotationTypes)
      }
    }

    when:
    def result = runner.runClass(spec)

    then:
    result.runCount == runCount
    result.failureCount == 0
    result.ignoreCount == 0

    where:
    annotationTypes << [[Slow], [Fast], [Slow, Fast]]
    runCount        << [1,      1,      2           ]
  }

  def "exclude methods based on annotations"() {
    runner.configurationScript = {
      runner {
        exclude(*annotationTypes)
      }
    }

    when:
    def result = runner.runClass(spec)

    then:
    result.runCount == runCount
    result.failureCount == 0
    result.ignoreCount == 0

    where:
    annotationTypes << [[Slow], [Fast], [Slow, Fast]]
    runCount        << [2,      2,      1           ]
  }

  def "include and exclude features based on annotations"() {
    runner.configurationScript = {
      runner {
        include(*annTypes1)
        exclude(*annTypes2)
      }
    }

    when:
    def result = runner.runClass(spec)

    then:
    result.runCount == runCount
    result.failureCount == 0
    result.ignoreCount == (runCount == 0 ? 1 : 0) // cannot prevent JUnit from running excluded specs, so they get ignored

    where:
    annTypes1   << [[Slow], [Slow], [Slow],       [Fast], [Fast], [Fast],       [Slow, Fast], [Slow, Fast], [Slow, Fast]]
    annTypes2   << [[Slow], [Fast], [Slow, Fast], [Slow], [Fast], [Slow, Fast], [Slow],       [Fast],       [Slow, Fast]]
    runCount    << [0,      1,      0,            1,      0,      0,            1,            1,            0           ]
  }
}





