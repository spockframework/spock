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
import spock.lang.SpockTypeChecked

import static groovy.transform.TypeCheckingMode.SKIP

class IncludeExcludeSpecsWithInheritance extends EmbeddedSpecification {
  List specs

  def setup() {
    compiler.addPackageImport(getClass().package)

    specs = compiler.compileWithImports("""
@Slow
class Spec1 extends Specification {
  def feature1() {
    expect: true
  }
}

@Fast
class Spec2 extends Spec1 {
  def feature2() {
    expect: true
  }
}
    """)
  }

  @SpockTypeChecked(SKIP)
  private includeAndExcludeByAnnotations(Map args) {
    runner.configurationScript = {
      runner {
        if (args.includeAnnotations) {
          include(*args.includeAnnotations)
        }
        if (args.excludeAnnotations) {
          exclude(*args.excludeAnnotations)
        }
      }
    }
  }

  def "include specs based on annotations"() {
    given:
    includeAndExcludeByAnnotations(includeAnnotations: annotationTypes)

    when:
    def result = runner.runClasses(specs)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    where:
    annotationTypes << [[Slow], [Fast], [Slow, Fast]]
    runCount        << [1,      2,      3           ]
    ignoreCount     << [1,      1,      0           ]
  }

  def "exclude specs based on annotations"() {
    given:
    includeAndExcludeByAnnotations(excludeAnnotations: annotationTypes)

    when:
    def result = runner.runClasses(specs)

    then:
    result.testsSucceededCount == runCount
    result.testsFailedCount == 0
    result.testsSkippedCount == 0

    where:
    annotationTypes << [[Slow], [Fast], [Slow, Fast]]
    runCount        << [2,      1,      0           ]
    ignoreCount     << [1,      1,      2           ]
  }
}
