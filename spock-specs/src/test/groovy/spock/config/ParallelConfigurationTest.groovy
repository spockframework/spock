package spock.config


import org.spockframework.runtime.*
import org.spockframework.runtime.model.parallel.ExecutionMode
import spock.lang.*

class ParallelConfigurationTest extends Specification implements ConfigSupport {
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
      defaultClassesExecutionMode == ExecutionMode.CONCURRENT
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
          defaultClassesExecutionMode ExecutionMode.SAME_THREAD
          defaultExecutionMode ExecutionMode.SAME_THREAD
        }
      }
    }
    when:
    builder.build(configurationRegistry, closureToScript(closure))

    then:
    verifyAll(runnerConfiguration.parallel) {
      enabled
      defaultClassesExecutionMode == ExecutionMode.SAME_THREAD
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

  def "check dynamicWithReservedThreads"() {
    given:
    int expectedParallelism = Math.max(availableProcessors - 1, 1)
    Closure closure = {
      runner {
        parallel {
          enabled true
          dynamicWithReservedThreads(1, 1)
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

  def "dynamicWithReservedThreads throws for negative reservedThreads"() {
    given:
    int expectedParallelism = Math.max(availableProcessors - 1, 1)
    Closure closure = {
      runner {
        parallel {
          enabled true
          dynamicWithReservedThreads(1, -1)
        }
      }
    }

    when:
    builder.build(configurationRegistry, closureToScript(closure))

    then:
    thrown(IllegalArgumentException)
  }


  def "dynamicWithReservedThreads throws for factor larger than 1"() {
    given:
    int expectedParallelism = Math.max(availableProcessors - 1, 1)
    Closure closure = {
      runner {
        parallel {
          enabled true
          dynamicWithReservedThreads(2, 1)
        }
      }
    }

    when:
    builder.build(configurationRegistry, closureToScript(closure))

    then:
    thrown(IllegalArgumentException)
  }

}
