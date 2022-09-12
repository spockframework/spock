package org.spockframework.runtime.model

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.*
import spock.lang.Issue

class SpecInfoSpec extends EmbeddedSpecification {

  @Issue("https://github.com/spockframework/spock/issues/1512")
  def "multiple fixture methods are not supported"() {
    given:
    def classes = compiler.compileWithImports("""
      class ASpec extends  Specification {

        def $fixtureMethod(String param = 'default') {
          println param
        }

        def test() {
          expect:
          true
        }
      }
      """)

    when:
    new SpecInfoBuilder(classes.find()).build()

    then:
    SpockException ex = thrown()
    ex.message == "Multiple $kind methods found in 'apackage.ASpec'. Methods [private java.lang.Object apackage.ASpec.$fixtureMethod(), private java.lang.Object apackage.ASpec.$fixtureMethod(java.lang.String)]. There can only be one per specification class."

    where:
    fixtureMethod | kind
    'setup'       | MethodKind.SETUP
    'cleanup'     | MethodKind.CLEANUP
    'setupSpec'   | MethodKind.SETUP_SPEC
    'cleanupSpec' | MethodKind.CLEANUP_SPEC
  }
}
