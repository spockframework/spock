package aPackage
import spock.lang.*

class ASpec extends Specification {
/*--------- tag::snapshot[] ---------*/
public java.lang.Object foobar() {
    return 'foo'
}

public void $spock_feature_0_0() {
    try {
        org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
        org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
        java.lang.Object foobar
        java.lang.Throwable $spock_feature_throwable
        try {
            org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.WHEN, []))
            foobar = this.foobar()
            org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.WHEN, []))
            org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.THEN, []))
            try {
                org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'println(foobar)', 6, 3, null, this, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), 'println'), new java.lang.Object[]{$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), foobar)}, $spock_valueRecorder.realizeNas(4, false), false, 3)
            }
            catch (java.lang.Throwable $spock_condition_throwable) {
                org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'println(foobar)', 6, 3, null, $spock_condition_throwable)}
            finally {
            }
            org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.THEN, []))
        }
        catch (java.lang.Throwable $spock_tmp_throwable) {
            $spock_feature_throwable = $spock_tmp_throwable
            throw $spock_tmp_throwable
        }
        finally {
            try {
                if ( $spock_feature_throwable == null) {
                    org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.CLEANUP, []))
                }
                foobar.size()
                if ( $spock_feature_throwable == null) {
                    org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.CLEANUP, []))
                }
            }
            catch (java.lang.Throwable $spock_tmp_throwable) {
                if ( $spock_feature_throwable != null) {
                    $spock_feature_throwable.addSuppressed($spock_tmp_throwable)
                } else {
                    throw $spock_tmp_throwable
                }
            }
            finally {
            }
        }
        this.getSpecificationContext().getMockController().leaveScope()
    }
    finally {
        org.spockframework.runtime.SpockRuntime.clearCurrentBlock(this.getSpecificationContext())}
}
/*--------- end::snapshot[] ---------*/
}
