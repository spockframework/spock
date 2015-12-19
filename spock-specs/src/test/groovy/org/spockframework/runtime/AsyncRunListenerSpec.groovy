package org.spockframework.runtime

import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.model.IterationInfo

import spock.lang.Specification

class AsyncRunListenerSpec extends Specification {
  def delegate = Mock(IRunListener)
  def asyncListener = new AsyncRunListener("my-test-thread", delegate)

  def "replays events in correct order in a separate thread"() {
    def specInfo = new SpecInfoBuilder(getClass()).build()
    def featureInfo = specInfo.features[0]
    def iterationInfo = new IterationInfo(featureInfo, new HashMap<String, Object>(), 1)
    def errorInfo = new ErrorInfo(featureInfo.featureMethod, new Exception())

    when:
    asyncListener.start()
    asyncListener.beforeSpec(specInfo)
    asyncListener.beforeFeature(featureInfo)
    asyncListener.beforeIteration(iterationInfo)
    asyncListener.error(errorInfo)
    asyncListener.afterIteration(iterationInfo)
    asyncListener.afterFeature(featureInfo)
    asyncListener.featureSkipped(featureInfo)
    asyncListener.afterSpec(specInfo)
    asyncListener.specSkipped(specInfo)
    asyncListener.stop()

    then:
    1 * delegate.beforeSpec(specInfo) >> { checkThread() }
    then:
    1 * delegate.beforeFeature(featureInfo) >> { checkThread() }
    then:
    1 * delegate.beforeIteration(iterationInfo) >> { checkThread() }
    then:
    1 * delegate.error(errorInfo) >> { checkThread() }
    then:
    1 * delegate.afterIteration(iterationInfo) >> { checkThread() }
    then:
    1 * delegate.afterFeature(featureInfo) >> { checkThread() }
    then:
    1 * delegate.featureSkipped(featureInfo) >> { checkThread() }
    then:
    1 * delegate.afterSpec(specInfo) >> { checkThread() }
    then:
    1 * delegate.specSkipped(specInfo) >> { checkThread() }
    then:
    0 * _
  }

  private void checkThread() {
    assert Thread.currentThread().name == "my-test-thread"
  }
}
