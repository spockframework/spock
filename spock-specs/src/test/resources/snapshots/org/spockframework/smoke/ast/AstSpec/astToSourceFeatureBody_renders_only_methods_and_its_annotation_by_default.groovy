package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {
/*--------- tag::snapshot[] ---------*/
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    try {
        org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.SETUP, []))
        java.lang.Object nothing = null
        this.getSpecificationContext().getMockController().leaveScope()
    }
    finally {
        org.spockframework.runtime.SpockRuntime.clearCurrentBlock(this.getSpecificationContext())}
}
/*--------- end::snapshot[] ---------*/
  }
}
