package org.spockframework.smoke.ast.condition

import org.spockframework.EmbeddedSpecification
import org.spockframework.specs.extension.Snapshot
import org.spockframework.specs.extension.Snapshotter

class CollectionConditionAstSpec extends EmbeddedSpecification {
  @Snapshot(extension = 'groovy')
  Snapshotter snapshotter

  def "collection condition matchCollectionsAsSet is transformed correctly"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
        given:
        def x = [1]
        expect:
        x =~ [1]
    ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "collection condition matchCollectionsInAnyOrder is transformed correctly"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
        given:
        def x = [1]
        expect:
        x ==~ [1]
    ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "regex find conditions are transformed correctly"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
        given:
        def x = "[1]"
        expect:
        x =~ /\\d/
    ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "regex match conditions are transformed correctly"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
        given:
        def x = "a1b"
        expect:
        x ==~ /a\\db/
    ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }
}
