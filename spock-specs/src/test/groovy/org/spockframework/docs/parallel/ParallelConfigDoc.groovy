package org.spockframework.docs.parallel

import org.spockframework.runtime.IConfigurationRegistry
import spock.config.*
import spock.lang.*

class ParallelConfigDoc extends Specification implements ConfigSupport {
  @Shared
  int availableProcessors = Runtime.runtime.availableProcessors();

  @Subject
  RunnerConfiguration runnerConfiguration = new RunnerConfiguration()

  IConfigurationRegistry configurationRegistry = Stub() {
    getConfigurationByName('runner') >> runnerConfiguration
  }


  def "enable parallel"() {
    given:
    Closure closure = {
      // tag::enable[]
      runner {
        parallel {
          enabled true
        }
      }
      // end::enable[]
    }
    when:
    builder.build(configurationRegistry, closureToScript(closure))

    then:
    runnerConfiguration.parallel.enabled
  }

  def "enable and fixed"() {
    given:
    Closure closure = {
      // tag::fixed[]
      runner {
        parallel {
          enabled true
          fixed(4)
        }
      }
      // end::fixed[]
    }
    when:
    builder.build(configurationRegistry, closureToScript(closure))

    then:
    runnerConfiguration.parallel.enabled
    runnerConfiguration.parallel.parallelExecutionConfiguration.parallelism == 4
  }
}
