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

class BlocksAst extends EmbeddedSpecification {
  @Snapshot(extension = 'groovy')
  SpockSnapshotter snapshotter

  def "all observable blocks with empty labels"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
    given: ''
    expect: ''
    when: ''
    then: ''
    cleanup: ''
    where: ''
    combined: ''
    filter: ''
    ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }
  def "all observable blocks with labels and blocks"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
    given: 'given'
    and: 'and given'
    expect: 'expect'
    and: 'and expect'
    when: 'when'
    and: 'and when'
    then: 'then'
    and: 'and then'
    then: 'then2'
    and: 'and then2'
    cleanup: 'cleanup'
    and: 'and cleanup'
    where: 'where'
    combined: 'combine'
    filter: 'only one execution'
    ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "all observable blocks with GString labels"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
    int idx = 0
    given: "given ${idx++}"
    expect: "expect ${idx++}"
    when: "when ${idx++}"
    then: "then ${idx++}"
    ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }
}
