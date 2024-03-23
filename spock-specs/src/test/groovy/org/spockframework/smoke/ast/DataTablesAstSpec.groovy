/*
 * Copyright 2023 the original author or authors.
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

package org.spockframework.smoke.ast

import org.spockframework.EmbeddedSpecification
import org.spockframework.specs.extension.SpockSnapshotter
import spock.lang.Snapshot
import spock.util.Show

class DataTablesAstSpec extends EmbeddedSpecification {
  @Snapshot(extension = 'groovy')
  SpockSnapshotter snapshotter

  def 'data tables with #separators can be combined'() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody """
      expect:
      true

      where:
      a $sepA _
      1 $sepA _
      2 $sepA _
      combined:
      b $sepB _
      3 $sepB _
      combined:
      c $sepA _
      4 $sepA _
      5 $sepA _
      6 $sepA _
    """, EnumSet.of(Show.METHODS)

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()

    where:
    sepA | sepB | separators
    '|'  | '|'  | 'pipes'
    ';'  | ';'  | 'semicolons'
    '|'  | ';'  | 'mixed separators'
  }

  def 'filter block becomes its own method'() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody '''
      expect:
      true

      where:
      a | _
      1 | _
      2 | _

      combined:

      b | _
      3 | _

      filter:
      a == 1
      b == 2
    ''', EnumSet.of(Show.METHODS)

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }
}
