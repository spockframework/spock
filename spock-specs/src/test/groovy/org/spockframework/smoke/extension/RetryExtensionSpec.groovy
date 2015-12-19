package org.spockframework.smoke.extension

import org.junit.ComparisonFailure
import org.spockframework.EmbeddedSpecification


class RetryExtensionSpec extends EmbeddedSpecification {

  def "if no failure, test should be run only once"(){
    when:
    runner.runSpecBody("""
      @Shared int runCount = 0;

      @Retry(5)
      def test(){
        runCount++;
        expect:
          runCount == 1
      }
    """)

    then:
    noExceptionThrown()
  }

  def "if there are failures, should restart iteration (max retries is 1 + Retry.value)"(){
    when:
    runner.runSpecBody("""
      @Shared int runCount = 0;

      @Retry(5)
      def test(){
        runCount++;
        expect:
          runCount == 1 + 5
      }
    """)

    then:
    noExceptionThrown()
  }

  def "if there are failures, should restart iteration and report last failure if all attempts was exceeded"(){
    when:
    runner.runSpecBody("""
      @Shared int runCount = 0;

      @Retry(5)
      def test(){
        runCount++;
        expect:
          runCount == 1 + 5 + 1 // should never reach this value
      }
    """)

    then:
    ComparisonFailure failure = thrown()
    failure.actual.trim() == "6"
    failure.expected.trim() == "7"
  }

  def "method level annotation should override spec level "(){
    when:
    runner.runWithImports("""
     @Retry(4)
     class Foo{
        @Shared int runCount1 = 0;
        @Shared int runCount2 = 0;

        @Retry(5)
        def test1(){
          runCount1++;
          expect:
            runCount1 == 1 + 5
        }

        def test2(){
          runCount2++;
          expect:
            runCount2 == 1 + 4
        }
      }
    """)

    then:
    noExceptionThrown()
  }
}
