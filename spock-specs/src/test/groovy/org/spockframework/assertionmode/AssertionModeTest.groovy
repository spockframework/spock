package org.spockframework.assertionmode
import org.spockframework.EmbeddedSpecification

class AssertionModeTest extends EmbeddedSpecification {

  def "class level @AssertionMode"() {
    when:
    def clazz = compiler.compileWithImports(
        """
                    @AssertionMode(AssertionType.CHECK_ALL_THEN_FAIL)
                    public class WithClassLevelAnnotation extends Specification {
                        def "test1"() {
                            expect:
                                1 == 2
                                3 == 4
                        }
                    }""")
    runner.throwFailure = false
    def result = runner.runClass(clazz[0])
    then:
    result.failures.size() == 2
    result.failures[0].exception.expected.trim() == "2"
    result.failures[0].exception.actual.trim() == "1"
    result.failures[1].exception.expected.trim() == "4"
    result.failures[1].exception.actual.trim() == "3"
  }

  def "method level @AssertionMode"() {
    when:
    def clazz = compiler.compileWithImports(
        """
                    public class WithClassLevelAnnotation extends Specification {
                        @AssertionMode(AssertionType.CHECK_ALL_THEN_FAIL)
                        def "test1"() {
                            expect:
                                1 == 2
                                3 == 4
                        }
                    }""")
    runner.throwFailure = false
    def result = runner.runClass(clazz[0])
    then:
    result.failures.size() == 2
    result.failures[0].exception.expected.trim() == "2"
    result.failures[0].exception.actual.trim() == "1"
    result.failures[1].exception.expected.trim() == "4"
    result.failures[1].exception.actual.trim() == "3"
  }

  def "method level overrides class level"() {
    when:
    def clazz = compiler.compileWithImports(
        """
                    @AssertionMode(AssertionType.CHECK_ALL_THEN_FAIL)
                    public class WithClassLevelAnnotation extends Specification {
                        @AssertionMode(AssertionType.FAIL_AFTER_FIRST_FAILURE)
                        def "test1"() {
                            expect:
                                1 == 2
                                3 == 4
                        }
                    }""")
    runner.throwFailure = false
    def result = runner.runClass(clazz[0])
    then:
    result.failures.size() == 1
    result.failures[0].exception.expected.trim() == "2"
    result.failures[0].exception.actual.trim() == "1"
  }

  def "chain `then` should not be merged"() {
    when:
    def clazz = compiler.compileWithImports(
        """
                    public class WithClassLevelAnnotation extends Specification {
                        @AssertionMode(AssertionType.CHECK_ALL_THEN_FAIL)
                        def "test1"() {
                            when:
                                def x = 2
                            then:
                                1 == 2
                            then:
                                3 == 4
                        }
                    }""")
    runner.throwFailure = false
    def result = runner.runClass(clazz[0])
    then:
    result.failures.size() == 1
    result.failures[0].exception.expected.trim() == "2"
    result.failures[0].exception.actual.trim() == "1"
  }

}
