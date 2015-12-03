package org.spockframework.verifyall
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.SpockTimeoutError

class VerifyAllSpecification extends EmbeddedSpecification {

  def "verifyAll"() {
    when:
    def clazz = compiler.compileWithImports(
        """
                    public class Test extends Specification {
                        def "test1"() {
                            expect:
                                verifyAll{
                                  1 == 2
                                  3 == 4
                                }
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

  def "assertion blocks should work as expected (reported only once)"() {
    when:
        def clazz = compiler.compileWithImports(
          """
                    import spock.util.concurrent.PollingConditions
                    public class Test extends Specification {
                        def "test1"() {
                            when:
                                def x = 2
                                def y = 3
                            then:
                                verifyAll {
                                    PollingConditions pollingConditions = new PollingConditions()
                                    pollingConditions.eventually {
                                      x == 3
                                      y == 4
                                    }
                                }
                        }
                    }""")
        runner.throwFailure = false
        def result = runner.runClass(clazz[0])
    then:
        result.failures.size() == 1
        result.failures[0].exception instanceof SpockTimeoutError
  }

  def "if exception is not in condition, all already failed conditions should be reported"(){
    when:
        def clazz = compiler.compileWithImports(
          """
                    import spock.util.concurrent.PollingConditions
                    public class Test extends Specification {
                        def "test1"() {
                            when:
                                def x = 2
                                def y = 3
                            then:
                                verifyAll {
                                    x == 3
                                    y == 4
                                    def urlWithSpaces = new URL("abc   ") //Invalid Url Exception
                                    urlWithSpaces != null
                                }
                        }
                    }""")
        runner.throwFailure = false
        def result = runner.runClass(clazz[0])
    then:
        result.failures.size() == 2
        result.failures[0].exception.expected.trim() == "3"
        result.failures[0].exception.actual.trim() == "2"
        result.failures[1].exception.expected.trim() == "4"
        result.failures[1].exception.actual.trim() == "3"
  }

}
