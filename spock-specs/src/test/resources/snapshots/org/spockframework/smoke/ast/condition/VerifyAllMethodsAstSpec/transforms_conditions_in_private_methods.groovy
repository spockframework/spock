package aPackage
import spock.lang.*

class ASpec extends Specification {
/*--------- tag::snapshot[] ---------*/
@spock.lang.VerifyAll
private void isPositive(int a) {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = new org.spockframework.runtime.ErrorCollector()
    try {
        org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
        try {
            org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'a > 0', 17, 5, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), a) > $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), 0)))
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'a > 0', 17, 5, null, $spock_condition_throwable)}
        finally {
        }
        try {
            org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder.reset(), '[true, false].any { it }', 18, 5, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), [$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), true), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), false)]), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(3), 'any'), new java.lang.Object[]{$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(4), { ->
                it
            })}, $spock_valueRecorder.realizeNas(7, false), false, 6)
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, '[true, false].any { it }', 18, 5, null, $spock_condition_throwable)}
        finally {
        }
        if (true) {
            try {
                org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder.reset(), '[true, false].any { it }', 20, 9, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), [$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), true), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), false)]), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(3), 'any'), new java.lang.Object[]{$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(4), { ->
                    it
                })}, $spock_valueRecorder.realizeNas(7, false), false, 6)
            }
            catch (java.lang.Throwable $spock_condition_throwable) {
                org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, '[true, false].any { it }', 20, 9, null, $spock_condition_throwable)}
            finally {
            }
        }
        try {
            this.with({ ->
                org.spockframework.runtime.ValueRecorder $spock_valueRecorder1 = new org.spockframework.runtime.ValueRecorder()
                try {
                    org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder1.reset(), '[true, false].any { it }', 23, 9, null, $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(2), [$spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(0), true), $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(1), false)]), $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(3), 'any'), new java.lang.Object[]{$spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(4), { ->
                        it
                    })}, $spock_valueRecorder1.realizeNas(7, false), false, 6)
                }
                catch (java.lang.Throwable $spock_condition_throwable) {
                    org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder1, '[true, false].any { it }', 23, 9, null, $spock_condition_throwable)}
                finally {
                }
                if (true) {
                    try {
                        org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder1.reset(), '[true, false].any { it }', 25, 13, null, $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(2), [$spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(0), true), $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(1), false)]), $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(3), 'any'), new java.lang.Object[]{$spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(4), { ->
                            it
                        })}, $spock_valueRecorder1.realizeNas(7, false), false, 6)
                    }
                    catch (java.lang.Throwable $spock_condition_throwable) {
                        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder1, '[true, false].any { it }', 25, 13, null, $spock_condition_throwable)}
                    finally {
                    }
                }
            })
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.groupConditionFailedWithException($spock_errorCollector, $spock_condition_throwable)}
        finally {
        }
    }
    finally {
        $spock_errorCollector.validateCollectedErrors()}
}

@org.spockframework.runtime.model.FeatureMetadata(name = 'foo', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    try {
        org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder.reset(), '[true, false].any { it }', 3, 5, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), [$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), true), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), false)]), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(3), 'any'), new java.lang.Object[]{$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(4), { ->
            it
        })}, $spock_valueRecorder.realizeNas(7, false), false, 6)
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, '[true, false].any { it }', 3, 5, null, $spock_condition_throwable)}
    finally {
    }
    if (true) {
        [true, false].any({ ->
            it
        })
    }
    this.with({ ->
        org.spockframework.runtime.ValueRecorder $spock_valueRecorder1 = new org.spockframework.runtime.ValueRecorder()
        try {
            org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder1.reset(), '[true, false].any { it }', 8, 9, null, $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(2), [$spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(0), true), $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(1), false)]), $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(3), 'any'), new java.lang.Object[]{$spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(4), { ->
                it
            })}, $spock_valueRecorder1.realizeNas(7, false), false, 6)
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder1, '[true, false].any { it }', 8, 9, null, $spock_condition_throwable)}
        finally {
        }
        if (true) {
            try {
                org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder1.reset(), '[true, false].any { it }', 10, 13, null, $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(2), [$spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(0), true), $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(1), false)]), $spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(3), 'any'), new java.lang.Object[]{$spock_valueRecorder1.record($spock_valueRecorder1.startRecordingValue(4), { ->
                    it
                })}, $spock_valueRecorder1.realizeNas(7, false), false, 6)
            }
            catch (java.lang.Throwable $spock_condition_throwable) {
                org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder1, '[true, false].any { it }', 10, 13, null, $spock_condition_throwable)}
            finally {
            }
        }
    })
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    this.getSpecificationContext().getMockController().leaveScope()
}
/*--------- end::snapshot[] ---------*/
}
