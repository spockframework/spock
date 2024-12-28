package apackage

import spock.lang.*

@org.spockframework.runtime.model.SpecMetadata(filename = 'script.groovy', line = 1)
public class apackage.ASpec extends spock.lang.Specification implements groovy.lang.GroovyObject {

    @org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = [])], parameterNames = [])
    public void $spock_feature_0_0() {
        org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
        java.lang.Object nothing = null
        org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
        this.getSpecificationContext().getMockController().leaveScope()
    }

}