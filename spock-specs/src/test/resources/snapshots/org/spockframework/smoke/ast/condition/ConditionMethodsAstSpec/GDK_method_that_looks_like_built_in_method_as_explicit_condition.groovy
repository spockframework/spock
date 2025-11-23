@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    try {
        org.spockframework.runtime.SpockRuntime.verifyMethodCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'null.with {\n  false\n}', 2, 8, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), null), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(1), 'with'), new java.lang.Object[]{$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(2), { ->
            false
        })}, $spock_valueRecorder.realizeNas(5, false), true, 4)
    }
    catch (java.lang.Throwable $spock_condition_throwable) {
        org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'null.with {\n  false\n}', 2, 8, null, $spock_condition_throwable)}
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    this.getSpecificationContext().getMockController().leaveScope()
}