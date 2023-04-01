package org.spockframework.smoke.ast.condition

import org.spockframework.EmbeddedSpecification
import org.spockframework.specs.extension.Snapshot
import org.spockframework.specs.extension.Snapshotter
import spock.lang.Issue
import spock.util.Show

class ExceptionConditionsAstSpec extends EmbeddedSpecification {
  @Snapshot(extension = 'groovy')
  Snapshotter snapshotter

  @Issue("https://github.com/spockframework/spock/issues/1266")
  def "thrown rewrite keeps correct method reference"() {
    given:
    snapshotter.specBody()

    when:
    def result = compiler.transpileSpecBody('''
def "cleanup blocks don't destroy method reference when invocation is assigned to variable with the same name"() {
  when:
  def foobar = foobar()

  then:
  thrown(IllegalStateException)
}

def foobar() {
  throw new IllegalStateException("foo")
}''', EnumSet.of(Show.METHODS))

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  @Issue("https://github.com/spockframework/spock/issues/1332")
  def "thrown rewrite keeps correct method reference for multi-assignments"() {
    given:
    snapshotter.specBody()

    when:
    def result = compiler.transpileSpecBody('''
def "cleanup blocks don't destroy method reference when invocation is assigned to variable with the same name"() {
  when:
  def (foobar, b) = foobar()

  then:
  thrown(IllegalStateException)
}

def foobar() {
  throw new IllegalStateException("foo")
}''', EnumSet.of(Show.METHODS))

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }
}
