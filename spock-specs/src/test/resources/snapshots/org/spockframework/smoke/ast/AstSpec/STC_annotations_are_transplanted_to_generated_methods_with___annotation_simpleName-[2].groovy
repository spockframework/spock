@org.spockframework.runtime.model.SpecMetadata(filename = 'script.groovy', line = 1)
public class apackage.ASpec extends spock.lang.Specification {

    @spock.lang.SpockCompileStatic
    @groovy.transform.CompileStatic
    @org.spockframework.runtime.model.FeatureMetadata(name = 'type-checked where block', ordinal = 0, line = 2, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.EXPECT, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.WHERE, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.FILTER, texts = [])], parameterNames = ['bar', 'baz', 'bam'])
    public void $spock_feature_0_0(java.lang.Object bar, java.lang.Object baz, java.lang.Object bam) {
        org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
        org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
        org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
        try {
            org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), 'true', 5, 5, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), true))
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, 'true', 5, 5, null, $spock_condition_throwable)}
        org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
        this.getSpecificationContext().getMockController().leaveScope()
    }

    @org.spockframework.runtime.model.DataProviderMetadata(line = 10, dataVariables = ['baz'])
    @groovy.transform.CompileStatic
    public java.lang.Object $spock_feature_0_0prov0(java.lang.Object foo) {
        return [[].does_not_exist()]
    }

    @org.spockframework.runtime.model.DataProviderMetadata(line = 12, dataVariables = ['bam'])
    @groovy.transform.CompileStatic
    public java.lang.Object $spock_feature_0_0prov1(java.lang.Object foo) {
        return [[].does_not_exist()]
    }

    @org.spockframework.runtime.model.DataProcessorMetadata(dataVariables = ['bar', 'baz', 'bam'])
    @groovy.transform.CompileStatic
    public java.lang.Object $spock_feature_0_0proc(java.lang.Object $spock_p0, java.lang.Object $spock_p1, java.lang.Object foo) {
        java.lang.Object bar = (([].does_not_exist()) as java.lang.Object)
        java.lang.Object baz = ($spock_p0 as java.lang.Object)
        java.lang.Object bam = ($spock_p1 as java.lang.Object)
        return new java.lang.Object[]{bar, baz, bam}
    }

    @groovy.transform.CompileStatic
    public java.lang.Object[] $spock_feature_0_0wherevars() {
        java.lang.Object[] $spock_whereVariableValues = new java.lang.Object[1]{}
        try {
            java.lang.Object foo = [].does_not_exist()
            $spock_whereVariableValues[0] = foo
            return $spock_whereVariableValues
        }
        catch (java.lang.Throwable $spock_tmp_throwable) {
            org.spockframework.runtime.SpockRuntime.closeWhereBlockVariablesAfterFailure($spock_whereVariableValues, $spock_tmp_throwable)
            throw $spock_tmp_throwable
        }
    }

    @groovy.transform.CompileStatic
    public static org.spockframework.runtime.model.DataVariableMultiplication[] $spock_feature_0_0prods() {
        return new org.spockframework.runtime.model.DataVariableMultiplication[]{new org.spockframework.runtime.model.DataVariableMultiplication(new java.lang.String[]{'baz', 'bam'}, new org.spockframework.runtime.model.DataVariableMultiplicationFactor(new java.lang.String[]{'baz'}), new org.spockframework.runtime.model.DataVariableMultiplicationFactor(new java.lang.String[]{'bam'}))}
    }

    @groovy.transform.CompileStatic
    public void $spock_feature_0_0filter(java.lang.Object bar, java.lang.Object baz, java.lang.Object bam, java.lang.Object foo) {
        org.spockframework.runtime.ErrorCollector $spock_errorCollector = org.spockframework.runtime.ErrorRethrower.INSTANCE
        org.spockframework.runtime.ValueRecorder $spock_valueRecorder = new org.spockframework.runtime.ValueRecorder()
        try {
            org.spockframework.runtime.SpockRuntime.verifyCondition($spock_errorCollector, $spock_valueRecorder.reset(), '[[].does_not_exist()]', 14, 5, null, $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(4), [$spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(3), $spock_valueRecorder.record($spock_valueRecorder.startRecordingValue(0), []).does_not_exist())]))
        }
        catch (java.lang.Throwable $spock_condition_throwable) {
            org.spockframework.runtime.SpockRuntime.conditionFailedWithException($spock_errorCollector, $spock_valueRecorder, '[[].does_not_exist()]', 14, 5, null, $spock_condition_throwable)}
    }

}