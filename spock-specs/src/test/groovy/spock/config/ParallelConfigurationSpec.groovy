package spock.config


import org.spockframework.runtime.IConfigurationRegistry
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.*

class ParallelConfigurationSpec extends Specification implements ConfigSupport {
  @Shared
  int availableProcessors = Runtime.runtime.availableProcessors();

  @Subject
  RunnerConfiguration runnerConfiguration = new RunnerConfiguration()

  IConfigurationRegistry configurationRegistry = Stub() {
    getConfigurationByName('runner') >> runnerConfiguration
  }

  def "check default values"() {
    expect:
    verifyAll(runnerConfiguration.parallel) {
      enabled == Boolean.getBoolean("spock.parallel.enabled")
      defaultSpecificationExecutionMode == ExecutionMode.CONCURRENT
      defaultExecutionMode == ExecutionMode.CONCURRENT
      parallelExecutionConfiguration.parallelism == Math.max(availableProcessors - 2, 1)
    }
  }

  def "check binding"() {
    given:
    Closure closure = {
      runner {
        parallel {
          enabled true
          defaultSpecificationExecutionMode ExecutionMode.SAME_THREAD
          defaultExecutionMode ExecutionMode.SAME_THREAD
        }
      }
    }
    when:
    builder.build(configurationRegistry, closureToScript(closure))

    then:
    verifyAll(runnerConfiguration.parallel) {
      enabled
      defaultSpecificationExecutionMode == ExecutionMode.SAME_THREAD
      defaultExecutionMode == ExecutionMode.SAME_THREAD
    }
  }

  def "check fixed"() {
    given:
    Closure closure = {
      runner {
        parallel {
          enabled true
          fixed(55)
        }
      }
    }

    when:
    builder.build(configurationRegistry, closureToScript(closure))

    then:
    verifyAll(runnerConfiguration.parallel.parallelExecutionConfiguration) {
      parallelism == 55
      maxPoolSize == 55 + 256
      minimumRunnable == 55
      corePoolSize == 55
      keepAliveSeconds == 30
    }
  }

  def "check dynamic"() {
    given:
    int expectedParallelism = availableProcessors * 2
    Closure closure = {
      runner {
        parallel {
          enabled true
          dynamic(2)
        }
      }
    }

    when:
    builder.build(configurationRegistry, closureToScript(closure))

    then:
    verifyAll(runnerConfiguration.parallel.parallelExecutionConfiguration) {
      parallelism == expectedParallelism
      maxPoolSize == expectedParallelism + 256
      minimumRunnable == expectedParallelism
      corePoolSize == expectedParallelism
      keepAliveSeconds == 30
    }
  }

  def "check dynamicWithReservedProcessors"() {
    given:
    int expectedParallelism = Math.max(availableProcessors - 1, 1)
    Closure closure = {
      runner {
        parallel {
          enabled true
          dynamicWithReservedProcessors(1, 1)
        }
      }
    }

    when:
    builder.build(configurationRegistry, closureToScript(closure))

    then:
    verifyAll(runnerConfiguration.parallel.parallelExecutionConfiguration) {
      parallelism == expectedParallelism
      maxPoolSize == expectedParallelism + 256
      minimumRunnable == expectedParallelism
      corePoolSize == expectedParallelism
      keepAliveSeconds == 30
    }
  }

  def "dynamicWithReservedProcessors throws for negative reservedThreads"() {
    given:
    int expectedParallelism = Math.max(availableProcessors - 1, 1)
    Closure closure = {
      runner {
        parallel {
          enabled true
          dynamicWithReservedProcessors(1, -1)
        }
      }
    }

    when:
    builder.build(configurationRegistry, closureToScript(closure))

    then:
    thrown(IllegalArgumentException)
  }


  def "dynamicWithReservedProcessors throws for factor larger than 1"() {
    given:
    int expectedParallelism = Math.max(availableProcessors - 1, 1)
    Closure closure = {
      runner {
        parallel {
          enabled true
          dynamicWithReservedProcessors(2, 1)
        }
      }
    }

    when:
    builder.build(configurationRegistry, closureToScript(closure))

    then:
    thrown(IllegalArgumentException)
  }

}
