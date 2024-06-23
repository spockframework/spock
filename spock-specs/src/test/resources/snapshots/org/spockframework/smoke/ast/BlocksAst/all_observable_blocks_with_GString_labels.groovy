package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {
/*--------- tag::snapshot[] ---------*/
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.WHEN, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.THEN, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    java.lang.Integer idx = 0
    org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), 0)
    "given ${( idx )++}"
    org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), 0)
    org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), 1)
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), '\"expect \${idx++}\"', 3, 13, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), "${$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), ( idx )++)}expect "))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, '\"expect \${idx++}\"', 3, 13, null, $spock_condition_throwable)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), 1)
    org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), 2)
    "when ${( idx )++}"
    org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), 2)
    org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), 3)
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), '\"then \${idx++}\"', 5, 11, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), "${$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), ( idx )++)}then "))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, '\"then \${idx++}\"', 5, 11, null, $spock_condition_throwable)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), 3)
    this.getSpecificationContext().getMockController().leaveScope()
}
/*--------- end::snapshot[] ---------*/
  }
}
