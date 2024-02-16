/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.smoke.ast

import org.spockframework.EmbeddedSpecification
import org.spockframework.specs.extension.SpockSnapshotter
import spock.lang.Snapshot

class DataAstSpec extends EmbeddedSpecification {
  @Snapshot(extension = 'groovy')
  SpockSnapshotter snapshotter

  def "multi-parameterization"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
    expect: a == b
    where:
    [a, b] << [[1, 1], [2, 2], [3, 3]]
  ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "nested multi-parameterization"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
    expect: a == b
    where:
    [a, [_, b]] << [[3, [1, 3]]]
  ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }
}
