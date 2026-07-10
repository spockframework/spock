package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {
/*--------- tag::snapshot[] ---------*/
public void $spock_feature_0_0(java.lang.Object x, java.lang.Object y, java.lang.Object z, java.lang.Object label, java.lang.Object total) {
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'true', 2, 5, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), true))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'true', 2, 5, null, $spock_condition_throwable)}
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    this.getSpecificationContext().getMockController().leaveScope()
}

public java.lang.Object $spock_feature_0_0prov0(java.lang.Object base, java.lang.Object sep) {
    return [1, 3]
}

public java.lang.Object $spock_feature_0_0prov1(java.util.List $spock_p_x, java.lang.Object base, java.lang.Object sep) {
    return [2, 4]
}

public java.lang.Object $spock_feature_0_0prov2(java.lang.Object base, java.lang.Object sep) {
    return [100, 200]
}

public java.lang.Object $spock_feature_0_0proc(java.lang.Object $spock_p0, java.lang.Object $spock_p1, java.lang.Object $spock_p2, java.lang.Object base, java.lang.Object sep) {
    java.lang.Object x = ($spock_p0 as java.lang.Object)
    java.lang.Object y = ($spock_p1 as java.lang.Object)
    java.lang.Object z = ($spock_p2 as java.lang.Object)
    java.lang.Object label = (("${x}${sep}${y}") as java.lang.Object)
    java.lang.Object total = ((x + y + base ) as java.lang.Object)
    return new java.lang.Object[]{x, y, z, label, total}
}

public java.lang.Object[] $spock_feature_0_0wherevars() {
    java.lang.Object[] $spock_whereVariableValues = new java.lang.Object[2]{}
    try {
        java.lang.Object base = 10
        $spock_whereVariableValues[0] = base
        java.lang.Object sep = '-'
        $spock_whereVariableValues[1] = sep
        return $spock_whereVariableValues
    }
    catch (java.lang.Throwable $spock_tmp_throwable) {
        org.spockframework.runtime.SpockRuntime.closeWhereBlockVariablesAfterFailure($spock_whereVariableValues, $spock_tmp_throwable)
        throw $spock_tmp_throwable
    }
}

public static org.spockframework.runtime.model.DataVariableMultiplication[] $spock_feature_0_0prods() {
    return new org.spockframework.runtime.model.DataVariableMultiplication[]{new org.spockframework.runtime.model.DataVariableMultiplication(new java.lang.String[]{'x', 'y', 'z'}, new org.spockframework.runtime.model.DataVariableMultiplicationFactor(new java.lang.String[]{'x', 'y'}), new org.spockframework.runtime.model.DataVariableMultiplicationFactor(new java.lang.String[]{'z'}))}
}

public void $spock_feature_0_0filter(java.lang.Object x, java.lang.Object y, java.lang.Object z, java.lang.Object label, java.lang.Object total, java.lang.Object base, java.lang.Object sep) {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'total > 0', 20, 5, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), total) > $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), 0)))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'total > 0', 20, 5, null, $spock_condition_throwable)}
}
/*--------- end::snapshot[] ---------*/
  }
}
