package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {
/*--------- tag::snapshot[] ---------*/
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), 0)
    java.lang.Object x = [1]
    org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), 0)
    org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), 1)
    try {
        org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'x =~ [1]', 4, 9, null, org.spockframework.runtime.SpockRuntime, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), 'matchCollectionsAsSet'), new java.lang.Object[]{$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), x), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(3), [$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), 1)])}, $spock_valueRecorder.realizeNas(6, false), false, 5)
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'x =~ [1]', 4, 9, null, $spock_condition_throwable)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), 1)
    this.getSpecificationContext().getMockController().leaveScope()
}
/*--------- end::snapshot[] ---------*/
  }
}
