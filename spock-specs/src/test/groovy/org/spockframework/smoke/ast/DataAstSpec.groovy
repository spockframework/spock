package org.spockframework.smoke.ast

import org.spockframework.EmbeddedSpecification
import org.spockframework.specs.extension.Snapshot
import org.spockframework.specs.extension.Snapshotter

class DataAstSpec extends EmbeddedSpecification {
  @Snapshot(extension = 'groovy')
  Snapshotter snapshotter

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
