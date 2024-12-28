package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {
/*--------- tag::snapshot[] ---------*/
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    java.lang.Object x = '[1]'
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 1)
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'x =~ /\\d/', 4, 9, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), org.spockframework.runtime.SpockRuntime.matchCollectionsAsSet($spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), x), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), '\\d'))))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'x =~ /\\d/', 4, 9, null, $spock_condition_throwable)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 1)
    this.getSpecificationContext().getMockController().leaveScope()
}
/*--------- end::snapshot[] ---------*/
  }
}
