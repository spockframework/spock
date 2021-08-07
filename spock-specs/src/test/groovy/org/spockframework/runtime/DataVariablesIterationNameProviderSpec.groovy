/*
 * Copyright 2020 the original author or authors.
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

package org.spockframework.runtime

import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.IterationInfo
import spock.lang.Specification
import spock.lang.Subject

class DataVariablesIterationNameProviderSpec extends Specification {
  @Subject
  DataVariablesIterationNameProvider testee = new DataVariablesIterationNameProvider()

  def feature = new FeatureInfo().tap {
    name = 'the feature'
    reportIterations = true
  }
  IterationInfo iteration = Stub {
    getFeature() >> feature
    getIterationIndex() >> 99
  }

  def 'returns feature name when not reporting iterations'() {
    given:
    feature.reportIterations = false

    expect:
    testee.getName(iteration) == feature.name
  }

  def 'returns feature name with data variables and iteration index when reporting iterations'() {
    given:
    iteration.getDataVariables() >> [x: 1, y: 2, z: 3]

    expect:
    testee.getName(iteration) == "$feature.name [x: 1, y: 2, z: 3, #$iteration.iterationIndex]"
  }

  def 'renders data variables in Groovy style'() {
    given:
    iteration.getDataVariables() >> [x: [1], y: [a: 2], z: [3] as int[]]

    expect:
    testee.getName(iteration) == "$feature.name [x: [1], y: [a:2], z: [3], #$iteration.iterationIndex]"
  }

  def 'returns feature name with iteration index when reporting iterations but data variables are null'() {
    given:
    iteration.getDataVariables() >> null

    expect:
    testee.getName(iteration) == "$feature.name [#$iteration.iterationIndex]"
  }

  def 'handles errors during toString rendering gracefully'() {
    given:
    def erroneousObject = new Object() {
      @Override
      String toString() {
        throw new RuntimeException("Don't look at me like that")
      }
    }
    iteration.getDataVariables() >> [x: erroneousObject, y: 2, z: 3]

    expect:
    testee.getName(iteration) == "$feature.name [x: #Error:RuntimeException during rendering, y: 2, z: 3, #$iteration.iterationIndex]"
  }


  def 'returns data variables and iteration index when reporting iterations and includeFeatureNameForIterations=false'() {
    given:
    testee = new DataVariablesIterationNameProvider(false)
    iteration.getDataVariables() >> [x: 1, y: 2, z: 3]

    expect:
    testee.getName(iteration) == "x: 1, y: 2, z: 3, #$iteration.iterationIndex"
  }

  def 'renders data variables in Groovy style and includeFeatureNameForIterations=false'() {
    given:
    testee = new DataVariablesIterationNameProvider(false)
    iteration.getDataVariables() >> [x: [1], y: [a: 2], z: [3] as int[]]

    expect:
    testee.getName(iteration) == "x: [1], y: [a:2], z: [3], #$iteration.iterationIndex"
  }

  def 'returns iteration index when reporting iterations but data variables are null and includeFeatureNameForIterations=false'() {
    given:
    testee = new DataVariablesIterationNameProvider(false)
    iteration.getDataVariables() >> null

    expect:
    testee.getName(iteration) == "#$iteration.iterationIndex"
  }
}
