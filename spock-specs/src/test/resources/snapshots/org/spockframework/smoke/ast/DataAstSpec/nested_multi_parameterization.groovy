package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {
/*--------- tag::snapshot[] ---------*/
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.WHERE, texts = [])], parameterNames = ['a', 'b'])
public void $spock_feature_0_0(java.lang.Object a, java.lang.Object b) {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'a == b', 1, 83, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), a) == $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), b)))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'a == b', 1, 83, null, $spock_condition_throwable)}
    finally {
    }
    this.getSpecificationContext().getMockController().leaveScope()
}

@org.spockframework.runtime.model.DataProviderMetadata(line = 3, dataVariables = ['a', 'b'])
public java.lang.Object $spock_feature_0_0prov0() {
    return [[3, [1, 3]]]
}

@org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ['a', 'b'])
public java.lang.Object $spock_feature_0_0proc(java.lang.Object $spock_p0) {
    java.lang.Object a = (( $spock_p0 instanceof java.util.Map ? $spock_p0.getAt('a') : $spock_p0.getAt(0)) as java.lang.Object)
    java.lang.Object $spock_l0 = (($spock_p0.getAt(1)) as java.lang.Object)
    java.lang.Object b = (( $spock_l0 instanceof java.util.Map ? $spock_l0.getAt('b') : $spock_l0.getAt(1)) as java.lang.Object)
    return new java.lang.Object[]{ a , b }
}
/*--------- end::snapshot[] ---------*/
  }
}
