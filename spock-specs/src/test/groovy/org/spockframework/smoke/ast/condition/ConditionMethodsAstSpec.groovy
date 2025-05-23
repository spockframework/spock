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

package org.spockframework.smoke.ast.condition


import org.spockframework.EmbeddedSpecification
import org.spockframework.specs.extension.SpockSnapshotter
import spock.lang.Snapshot

class ConditionMethodsAstSpec extends EmbeddedSpecification {
  @Snapshot(extension = 'groovy')
  SpockSnapshotter snapshotter

  def "GDK method that looks like built-in method as implicit condition"() {
    when:
    def result = compiler.transpileFeatureBody('''
expect:
null.with {
  false
}
''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "GDK method that looks like built-in method as explicit condition"() {
    when:
    def result = compiler.transpileFeatureBody('''
expect:
assert null.with {
  false
}
''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "condition method #conditionMethod within condition method #conditionMethod"() {
    when:
    def result = compiler.transpileFeatureBody("""
expect:
$conditionMethod(['']) {
  $conditionMethod(['']) {
    false
  }
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()

    where:
    conditionMethod << [
      'with',
      'verifyAll',
      'verifyEach'
    ]
  }

  def "condition method #conditionMethod within condition method #conditionMethod with exception"() {
    when:
    def result = compiler.transpileFeatureBody("""
expect:
$conditionMethod(['']) {
  $conditionMethod(['']) {
    true
    throw new Exception('foo')
  }
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()

    where:
    conditionMethod << [
      'with',
      'verifyAll',
      'verifyEach'
    ]
  }

  def "condition method #conditionMethod within condition method #conditionMethod with only exception"() {
    when:
    def result = compiler.transpileFeatureBody("""
expect:
$conditionMethod(['']) {
  $conditionMethod(['']) {
    throw new Exception('foo')
  }
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()

    where:
    conditionMethod << [
      'with',
      'verifyAll',
      'verifyEach'
    ]
  }

  def "condition method #conditionMethod"() {
    when:
    def result = compiler.transpileFeatureBody("""
expect:
$conditionMethod(['']) {
  false
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()

    where:
    conditionMethod << [
      'with',
      'verifyAll',
      'verifyEach'
    ]
  }

  def "condition method #conditionMethod with exception"() {
    when:
    def result = compiler.transpileFeatureBody("""
expect:
$conditionMethod(['']) {
  true
  throw new Exception('foo')
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()

    where:
    conditionMethod << [
      'with',
      'verifyAll',
      'verifyEach'
    ]
  }

  def "condition method #conditionMethod with only exception"() {
    when:
    def result = compiler.transpileFeatureBody("""
expect:
$conditionMethod(['']) {
  throw new Exception('foo')
}
""")

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()

    where:
    conditionMethod << [
      'with',
      'verifyAll',
      'verifyEach'
    ]
  }
}
