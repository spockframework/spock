package org.spockframework.docs.parallel

import org.spockframework.runtime.*
import spock.config.*
import spock.lang.*
import spock.util.EmbeddedSpecRunner

import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder

import static org.spockframework.runtime.model.parallel.ExecutionMode.CONCURRENT
import static org.spockframework.runtime.model.parallel.ExecutionMode.SAME_THREAD

class ParallelConfigDoc extends Specification implements ConfigSupport {
  @Shared
  UniqueId engineId = UniqueId.forEngine("spock")

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

  def "execution mode is calculated correctly"() {
    given: 'a runner configuration'
    EmbeddedSpecRunner embeddedSpecRunner = new EmbeddedSpecRunner()
    embeddedSpecRunner.configurationScript {
      runner {
        parallel {
          enabled true
          defaultExecutionMode defExecutionMode
          defaultSpecificationExecutionMode defSpecificationExecutionMode
        }
      }
    }
    def discoveryRequest = createRequest(spec)

    when: 'running the discovery'
    def testDescriptor = embeddedSpecRunner.withNewContext {
      new SpockEngine().discover(discoveryRequest, engineId)
    }

    then: 'unpacking the descriptors'
    def classDescriptor = testDescriptor.children[0] as SpecNode
    def simpleNode = classDescriptor.children[0] as SimpleFeatureNode
    def parameterizedNode = classDescriptor.children[1] as ParameterizedFeatureNode

    and: 'checking if execution modes are as expected'
    classDescriptor.executionMode.name() == specExecutionMode.name()
    simpleNode.executionMode.name() == nodeExecutionMode.name()
    parameterizedNode.executionMode.name() == nodeExecutionMode.name()

    where:
    spec                 | defSpecificationExecutionMode | defExecutionMode || specExecutionMode | nodeExecutionMode
    ExampleParallelASpec | CONCURRENT                    | CONCURRENT       || CONCURRENT        | CONCURRENT
    ExampleParallelASpec | CONCURRENT                    | SAME_THREAD      || CONCURRENT        | SAME_THREAD
    ExampleParallelASpec | SAME_THREAD                   | CONCURRENT       || SAME_THREAD       | CONCURRENT
    ExampleParallelASpec | SAME_THREAD                   | SAME_THREAD      || SAME_THREAD       | SAME_THREAD

    ExampleParallelBSpec | CONCURRENT                    | CONCURRENT       || SAME_THREAD       | SAME_THREAD
    ExampleParallelBSpec | CONCURRENT                    | SAME_THREAD      || SAME_THREAD       | SAME_THREAD
    ExampleParallelBSpec | SAME_THREAD                   | CONCURRENT       || SAME_THREAD       | SAME_THREAD
    ExampleParallelBSpec | SAME_THREAD                   | SAME_THREAD      || SAME_THREAD       | SAME_THREAD

    ExampleParallelCSpec | CONCURRENT                    | CONCURRENT       || CONCURRENT        | SAME_THREAD
    ExampleParallelCSpec | CONCURRENT                    | SAME_THREAD      || CONCURRENT        | SAME_THREAD
    ExampleParallelCSpec | SAME_THREAD                   | CONCURRENT       || SAME_THREAD       | SAME_THREAD
    ExampleParallelCSpec | SAME_THREAD                   | SAME_THREAD      || SAME_THREAD       | SAME_THREAD
  }

  LauncherDiscoveryRequest createRequest(Class<? extends Specification> clazz) {
    LauncherDiscoveryRequestBuilder.request()
      .selectors(DiscoverySelectors.selectClass(clazz))
      .build()
  }
}

class ExampleParallelASpec extends Specification {
  def "a test"() {
    expect: true
  }

  def "data test"() {
    expect: true
    where:
    i << (1..2)
  }
}

@Execution(SAME_THREAD)
class ExampleParallelBSpec extends Specification {
  def "a test"() {
    expect: true
  }

  def "data test"() {
    expect: true
    where:
    i << (1..2)
  }
}

class ExampleParallelCSpec extends Specification {
  @Execution(SAME_THREAD)
  def "a test"() {
    expect: true
  }

  @Execution(SAME_THREAD)
  def "data test"() {
    expect: true
    where:
    i << (1..2)
  }
}
