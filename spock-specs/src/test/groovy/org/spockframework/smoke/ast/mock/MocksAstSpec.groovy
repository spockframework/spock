package org.spockframework.smoke.ast.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.specs.extension.SpockSnapshotter
import spock.lang.Snapshot

class MocksAstSpec extends EmbeddedSpecification {

  @Snapshot(extension = 'groovy')
  SpockSnapshotter snapshotter

  def "simple interaction"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody("""
    given:
    List list = Mock()

    when:
    list.add(1)

    then:
    1 * list.add(1)
""")
    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }
}
