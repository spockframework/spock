package org.spockframework.mock

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockException
import spock.mock.AutoAttach

class AutoAttachExtensionSpec extends EmbeddedSpecification {

  def "setup"() {
    runner.addClassImport(AutoAttach)
  }

  def "null field"() {
    when:
    runner.runWithImports("""
      @Stepwise
      class NullFieldSpec extends Specification {
        @AutoAttach
        def field = null

        def "feature"()  { expect: true }
      }
    """)
    then:
    SpockException ex = thrown()
    ex.message == "Cannot AutoAttach 'null' for field field:3"
  }

  def "No mock value for field"() {
    when:
    runner.runWithImports("""
      @Stepwise
      class NoMockSpec extends Specification {
        @AutoAttach
        def field = "Value"

        def "feature"()  { expect: true }
      }
    """)
    then:
    SpockException ex = thrown()
    ex.message == "AutoAttach failed 'Value' is not a mock for field field:3"
  }
}
