package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {
/*--------- tag::snapshot[] ---------*/
public void $spock_feature_0_0(java.lang.Object a, java.lang.Object b) {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.EXPECT, []))
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'true', 2, 7, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), true))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'true', 2, 7, null, $spock_condition_throwable)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.EXPECT, []))
    org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.FILTER, []))
    org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.FILTER, []))
    this.getSpecificationContext().getMockController().leaveScope()
}

public java.lang.Object $spock_feature_0_0prov0() {
    return [1, 2]
}

public java.lang.Object $spock_feature_0_0prov1(java.util.List $spock_p_a) {
    return [3]
}

public java.lang.Object $spock_feature_0_0proc(java.lang.Object $spock_p0, java.lang.Object $spock_p1) {
    java.lang.Object a = (( $spock_p0 ) as java.lang.Object)
    java.lang.Object b = (( $spock_p1 ) as java.lang.Object)
    return new java.lang.Object[]{ a , b }
}

public static org.spockframework.runtime.model.DataVariableMultiplication[] $spock_feature_0_0prods() {
    return new org.spockframework.runtime.model.DataVariableMultiplication[]{new org.spockframework.runtime.model.DataVariableMultiplication(new java.lang.String[]{'a', 'b'}, new org.spockframework.runtime.model.DataVariableMultiplicationFactor(new java.lang.String[]{'a'}), new org.spockframework.runtime.model.DataVariableMultiplicationFactor(new java.lang.String[]{'b'}))}
}

public static void $spock_feature_0_0filter(java.lang.Object a, java.lang.Object b) {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'a == 1', 15, 7, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), a) == $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), 1)))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'a == 1', 15, 7, null, $spock_condition_throwable)}
    finally {
    }
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'b == 2', 16, 7, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), b) == $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), 2)))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'b == 2', 16, 7, null, $spock_condition_throwable)}
    finally {
    }
}
/*--------- end::snapshot[] ---------*/
  }
}
