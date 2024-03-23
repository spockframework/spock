package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {
/*--------- tag::snapshot[] ---------*/
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.WHEN, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.THEN, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    try {
        org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.SETUP, []))
        java.util.List list = this.MockImpl('list', java.util.List)
        this.getSpecificationContext().getMockController().enterScope()
        this.getSpecificationContext().getMockController().addInteraction(new org.spockframework.mock.runtime.InteractionBuilder(8, 5, '1 * list.add(1)').setFixedCount(1).addEqualTarget(list).addEqualMethodName('add').setArgListKind(true, false).addEqualArg(1).build())
        org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.SETUP, []))
        org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.WHEN, []))
        list.add(1)
        org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.WHEN, []))
        org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.THEN, []))
        this.getSpecificationContext().getMockController().leaveScope()
        org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.THEN, []))
        this.getSpecificationContext().getMockController().leaveScope()
    }
    finally {
        org.spockframework.runtime.SpockRuntime.clearCurrentBlock(this.getSpecificationContext())}
}
/*--------- end::snapshot[] ---------*/
  }
}
