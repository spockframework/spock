@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    this.verifyAll([''], { ->
        org.spockframework.runtime.ValueRecorder $spock_valueRecorder1 = new org.spockframework.runtime.ValueRecorder()
        org.spockframework.runtime.ErrorCollector $spock_errorCollector1 = new org.spockframework.runtime.ErrorCollector()
        try {
            try {
                this.verifyAll([''], { ->
                    throw new java.lang.Exception('foo')
                })
            }
            catch (java.lang.Throwable $spock_condition_throwable) {
                org.spockframework.runtime.SpockRuntime.groupConditionFailedWithException($spock_errorCollector1, $spock_condition_throwable)}
            finally {
            }
        }
        finally {
            $spock_errorCollector1.validateCollectedErrors()}
    })
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    this.getSpecificationContext().getMockController().leaveScope()
}