package org.spockframework.smoke.ast

import org.spockframework.EmbeddedSpecification
import org.spockframework.specs.extension.Snapshot
import org.spockframework.specs.extension.Snapshotter
import spock.lang.Issue
import spock.util.Show

@Snapshot(extension = 'groovy')
class CleanupBlocksAstSpec extends EmbeddedSpecification {
  Snapshotter snapshotter

  @Issue("https://github.com/spockframework/spock/issues/1266")
  def "cleanup rewrite keeps correct method reference"() {
    given:
    snapshotter.specBody()

    when:
    def result = compiler.transpileSpecBody('''
def "cleanup blocks don't destroy method reference when invocation is assigned to variable with the same name"() {
  when:
  def foobar = foobar()

  then:
  println(foobar)

  cleanup:
  foobar.size()
}

def foobar() {
  return "foo"
}''', EnumSet.of(Show.METHODS))

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  @Issue("https://github.com/spockframework/spock/issues/1332")
  def "cleanup rewrite keeps correct method reference for multi-assignments"() {
    given:
    snapshotter.specBody()

    when:
    def result = compiler.transpileSpecBody('''
def "cleanup blocks don't destroy method reference when invocation is assigned to variable with the same name"() {
  when:
  def (foobar, b) = foobar()

  then:
  println(foobar)

  cleanup:
  foobar.size()
}

def foobar() {
  return ["foo", "bar"]
}''', EnumSet.of(Show.METHODS))

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }
}
