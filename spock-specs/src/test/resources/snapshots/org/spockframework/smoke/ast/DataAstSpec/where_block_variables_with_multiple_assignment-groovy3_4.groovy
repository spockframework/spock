package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {
/*--------- tag::snapshot[] ---------*/
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.WHERE, texts = [])], parameterNames = ['name', 'greeting'])
public void $spock_feature_0_0(java.lang.Object name, java.lang.Object greeting) {
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'greeting == \"Hello, world!\"', 1, 83, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), greeting) == $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), 'Hello, world!')))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'greeting == \"Hello, world!\"', 1, 83, null, $spock_condition_throwable)}
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    this.getSpecificationContext().getMockController().leaveScope()
}

@org.spockframework.runtime.model.DataProviderMetadata(line = 4, dataVariables = ['name'])
public java.lang.Object $spock_feature_0_0prov0(java.lang.Object prefix, java.lang.Object suffix) {
    return ['world']
}

@org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ['name', 'greeting'])
public java.lang.Object $spock_feature_0_0proc(java.lang.Object $spock_p0, java.lang.Object prefix, java.lang.Object suffix) {
    java.lang.Object name = (( $spock_p0 ) as java.lang.Object)
    java.lang.Object greeting = (( prefix + name + suffix ) as java.lang.Object)
    return new java.lang.Object[]{ name , greeting }
}

public java.lang.Object[] $spock_feature_0_0wherevars() {
    java.lang.Object[] $spock_whereVariableValues = new java.lang.Object[2]{}
    try {
        def (java.lang.Object prefix, java.lang.Object suffix) = ['Hello, ', '!']
        $spock_whereVariableValues [ 0] = prefix
        $spock_whereVariableValues [ 1] = suffix
        return $spock_whereVariableValues
    }
    catch (java.lang.Throwable $spock_tmp_throwable) {
        org.spockframework.runtime.SpockRuntime.closeWhereBlockVariablesAfterFailure($spock_whereVariableValues, $spock_tmp_throwable)
        throw $spock_tmp_throwable
    }
}
/*--------- end::snapshot[] ---------*/
  }
}
