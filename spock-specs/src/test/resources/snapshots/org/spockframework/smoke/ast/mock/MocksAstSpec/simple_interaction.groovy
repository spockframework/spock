package aPackage
import spock.lang.*

class ASpec extends Specification {
  def "aFeature"() {
/*--------- tag::snapshot[] ---------*/
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.WHEN, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.THEN, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this.getSpecificationContext(), 0)
    java.util.List list = this.MockImpl('list', java.util.List)
    this.getSpecificationContext().getMockController().enterScope()
    this.getSpecificationContext().getMockController().addInteraction(new org.spockframework.mock.runtime.InteractionBuilder(8, 5, '1 * list.add(1)').setFixedCount(1).addEqualTarget(list).addEqualMethodName('add').setArgListKind(true, false).addEqualArg(1).build())
    org.spockframework.runtime.SpockRuntime.callBlockExited(this.getSpecificationContext(), 0)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this.getSpecificationContext(), 1)
    list.add(1)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this.getSpecificationContext(), 1)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this.getSpecificationContext(), 2)
    this.getSpecificationContext().getMockController().leaveScope()
    org.spockframework.runtime.SpockRuntime.callBlockExited(this.getSpecificationContext(), 2)
    this.getSpecificationContext().getMockController().leaveScope()
}
/*--------- end::snapshot[] ---------*/
  }
}
