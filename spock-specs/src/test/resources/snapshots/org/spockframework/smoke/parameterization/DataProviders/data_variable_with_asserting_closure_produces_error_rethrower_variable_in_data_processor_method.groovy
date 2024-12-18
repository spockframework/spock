package aPackage
import spock.lang.*

class ASpec extends Specification {
/*--------- tag::snapshot[] ---------*/
public void $spock_feature_0_0(java.lang.Object dataPipe, java.lang.Object dataVariable) {
    this.getSpecificationContext().getMockController().leaveScope()
}

public java.lang.Object $spock_feature_0_0prov0() {
    return [null]
}

public java.lang.Object $spock_feature_0_0proc(java.lang.Object $spock_p0) {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    java.lang.Object dataPipe = (( $spock_p0 ) as java.lang.Object)
    java.lang.Object dataVariable = (({ ->
        org.spockframework.runtime.ValueRecorder $spock_valueRecorder1 = new org.spockframework.runtime.ValueRecorder()
        try {
            org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder1.reset(), 'true', 3, 31, null, $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(0), true))
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder1, 'true', 3, 31, null, $spock_condition_throwable)}
        finally {
        }
    }) as java.lang.Object)
    return new java.lang.Object[]{ dataPipe , dataVariable }
}
/*--------- end::snapshot[] ---------*/
}
