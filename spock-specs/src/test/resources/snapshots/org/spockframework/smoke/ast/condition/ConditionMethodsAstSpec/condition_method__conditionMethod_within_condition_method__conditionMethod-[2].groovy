@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    this.verifyEach([''], { ->
        org.spockframework.runtime.ValueRecorder $spock_valueRecorder1 = new org.spockframework.runtime.ValueRecorder()
        try {
            this.verifyEach([''], { ->
                org.spockframework.runtime.ValueRecorder $spock_valueRecorder2 = new org.spockframework.runtime.ValueRecorder()
                try {
                    org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder2.reset(), 'false', 4, 5, null, $spock_valueRecorder2.record($spock_valueRecorder2.startRecordingValue(0), false))
                }
                catch (java.lang.Throwable $spock_condition_throwable) {
                    org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder2, 'false', 4, 5, null, $spock_condition_throwable)}
                finally {
                }
            })
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.groupConditionFailedWithException($spock_errorCollector, $spock_condition_throwable)}
        finally {
        }
    })
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    this.getSpecificationContext().getMockController().leaveScope()
}