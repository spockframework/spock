package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {
/*--------- tag::snapshot[] ---------*/
public void $spock_feature_0_0(java.lang.Object a, java.lang.Object b, java.lang.Object result) {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    try {
        org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'a + b == result', 2, 9, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(4), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), a) + $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), b)) == $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(3), result)))
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'a + b == result', 2, 9, null, $spock_condition_throwable)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    this.getSpecificationContext().getMockController().leaveScope()
}

public java.lang.Object $spock_feature_0_0prov0() {
    return [1, 3]
}

public java.lang.Object $spock_feature_0_0prov1(java.util.List $spock_p_a) {
    return [2, 4]
}

public java.lang.Object $spock_feature_0_0prov2(java.util.List $spock_p_a, java.util.List $spock_p_b) {
    return [{ ->
        java.lang.Object a = $spock_p_a.get(0)
        java.lang.Object b = $spock_p_b.get(0)
        return a + b
    }.call(), { ->
        java.lang.Object a = $spock_p_a.get(1)
        return a + a
    }.call()]
}

public java.lang.Object $spock_feature_0_0proc(java.lang.Object $spock_p0, java.lang.Object $spock_p1, java.lang.Object $spock_p2) {
    java.lang.Object a = (( $spock_p0 ) as java.lang.Object)
    java.lang.Object b = (( $spock_p1 ) as java.lang.Object)
    java.lang.Object result = (( $spock_p2 ) as java.lang.Object)
    return new java.lang.Object[]{ a , b , result }
}
/*--------- end::snapshot[] ---------*/
  }
}
