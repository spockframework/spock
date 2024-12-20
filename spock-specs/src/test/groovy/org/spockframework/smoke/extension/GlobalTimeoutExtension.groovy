package org.spockframework.smoke.extension

import spock.lang.Isolated
import spock.timeout.BaseTimeoutExtensionSpecification

import java.time.Duration

import org.spockframework.runtime.SpockTimeoutError

@Isolated("The timings are quite tight and it can get flaky on weak machines if run in parallel.")
class GlobalTimeoutExtension extends BaseTimeoutExtensionSpecification {
  def "applies timeout to features"() {
    given:
    enableGlobalTimeout()

    when:
    runner.runFeatureBody("""
        given: Thread.sleep 99999999999
        expect: true
    """)

    then:
    thrown SpockTimeoutError
  }

  def "supports inheritance for features"() {
    given:
    enableGlobalTimeout()

    when:
    runner.runWithImports("""
        abstract class A extends Specification {
            def "a test"() {
                given: Thread.sleep 99999999999
                expect: true
            }
        }
        
        class B extends A {
            def "normal test"() {
                expect: true
            }
        }
    """)

    then:
    thrown SpockTimeoutError
  }

  def "applies timeout to fixtures"() {
    given:
    enableGlobalTimeout(true)

    when:
    runner.runSpecBody("""
        def "$fixtureMethod"() {
            Thread.sleep 99999999999
        }
        
        def "a test"() {
            expect: true
        }
    """)

    then:
    thrown SpockTimeoutError

    where:
    fixtureMethod << ["setupSpec", "setup", "cleanup", "cleanupSpec"]
  }

  def "supports inheritance for fixtures"() {
    given:
    enableGlobalTimeout(true)

    when:
    runner.runWithImports("""
        abstract class A extends Specification {
            def "$fixtureMethod"() {
                Thread.sleep 99999999999
            }
        }
        
        class B extends A {
            def "normal test"() {
                expect: true
            }
        }
    """)

    then:
    thrown SpockTimeoutError

    where:
    fixtureMethod << ["setupSpec", "setup", "cleanup", "cleanupSpec"]
  }

  def "can be overwritten by feature annotation"() {
    given:
    enableGlobalTimeout()

    when:
    runner.runSpecBody("""
        @Timeout(value = 10, unit = SECONDS)
        def "a test"() {
            given: 
            
            Thread.sleep 100
            expect: true
        }
    """)

    then:
    noExceptionThrown()
  }

  def "can be overwritten by class-level annotation"() {
    given:
    enableGlobalTimeout()

    when:
    runner.runWithImports("""
        @Timeout(value = 10, unit = SECONDS)
        class A extends Specification {
          def setupSpec() {
              Thread.sleep 25
          }
          
          def setup() {
              Thread.sleep 25
          }
        
          def "a test"() {
              given: 
              Thread.sleep 25
              expect: true
          }
          
          def cleanup() {
              Thread.sleep 25
          }
          
          def cleanupSpec() {
              Thread.sleep 25
          }
        }
    """)

    then:
    noExceptionThrown()
  }

  def "can exclude fixture methods from global timeout"() {
    given:
    enableGlobalTimeout(false, Duration.ofMillis(20))

    when:
    runner.runSpecBody("""
        def setupSpec() {
            Thread.sleep 25
        }
        
        def setup() {
            Thread.sleep 25
        }
      
        def "a test"() {
            expect: true
        }
        
        def cleanup() {
            Thread.sleep 25
        }
        
        def cleanupSpec() {
            Thread.sleep 25
        }
    """)

    then:
    noExceptionThrown()
  }

  def enableGlobalTimeout(boolean applyToFixtureMethods = false, Duration globalTimeoutDuration = Duration.ofNanos(1)) {
    runner.configurationScript {
      timeout {
        globalTimeout globalTimeoutDuration
        printThreadDumpsOnInterruptAttempts false
        applyGlobalTimeoutToFixtures applyToFixtureMethods
      }
    }
  }
}
