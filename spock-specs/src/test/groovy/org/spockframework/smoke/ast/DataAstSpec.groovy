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
import org.spockframework.runtime.GroovyRuntimeUtil
import org.spockframework.specs.extension.SpockSnapshotter
import spock.lang.Requires
import spock.lang.Snapshot
import spock.util.Show

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

  def "where-block variables"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
    expect: greeting == "Hello, world"
    where:
    final prefix = "Hello, "
    name << ["world"]
    greeting = prefix + name
  ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  @Requires(value = { GroovyRuntimeUtil.MAJOR_VERSION >= 3 }, reason = "'final (a, b) = ...' is only parseable by the Parrot parser (Groovy 3.0+)")
  def "where-block variables with multiple assignment"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
    expect: greeting == "Hello, world!"
    where:
    final (prefix, suffix) = ["Hello, ", "!"]
    name << ["world"]
    greeting = prefix + name + suffix
  ''')

    then:
    // Groovy 3.0 and 4.0 render the tuple declaration identically; Groovy 5.0 differs, so it gets its own snapshot
    snapshotter.assertThat(result.source).matchesSnapshot(GroovyRuntimeUtil.MAJOR_VERSION >= 5 ? "groovy5" : "groovy3-4")
  }

  def "where-block variables combined with data table, data pipe, derived variables, cross-multiplication and filter"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
    expect:
    true

    where:
    final base = 10
    final sep = "-"

    x | y
    1 | 2
    3 | 4

    combined:

    z << [100, 200]

    label = "${x}${sep}${y}"
    total = x + y + base

    filter:
    total > 0
  ''', EnumSet.of(Show.METHODS))

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }
}
