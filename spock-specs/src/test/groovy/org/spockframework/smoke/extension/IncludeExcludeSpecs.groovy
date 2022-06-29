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
  """)
  }

  def "include specs based on annotations"() {
    runner.configurationScript = {
      runner {
        include(*annotationTypes)
      }
    }

    when:
    def result = runner.runClasses(specs)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    where:
    annotationTypes << [[Slow], [Fast], [Slow, Fast]]
    runCount        << [1,      1,      2           ]
  }

  def "exclude specs based on annotations"() {
    runner.configurationScript = {
      runner {
        exclude(*annotationTypes)
      }
    }

    when:
    def result = runner.runClasses(specs)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    where:
    annotationTypes << [[Slow], [Fast], [Slow, Fast]]
    runCount        << [2,      2,      1           ]
  }

  def "include and exclude specs based on annotations"() {
    runner.configurationScript = {
      runner {
        include(*annTypes1)
        exclude(*annTypes2)
      }
    }

    when:
    def result = runner.runClasses(specs)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    where:
    annTypes1   << [[Slow], [Slow], [Slow],       [Fast], [Fast], [Fast],       [Slow, Fast], [Slow, Fast], [Slow, Fast]]
    annTypes2   << [[Slow], [Fast], [Slow, Fast], [Slow], [Fast], [Slow, Fast], [Slow],       [Fast],       [Slow, Fast]]
    runCount    << [0,      1,      0,            1,      0,      0,            1,            1,            0           ]
  }
}
