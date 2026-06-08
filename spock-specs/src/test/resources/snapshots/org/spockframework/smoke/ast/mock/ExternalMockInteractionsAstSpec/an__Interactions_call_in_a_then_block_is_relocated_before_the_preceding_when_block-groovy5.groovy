package aPackage
import spock.lang.*

class ASpec extends Specification {
/*--------- tag::snapshot[] ---------*/
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 8, blocks = [@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.WHEN, texts = []), @org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.THEN, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    java.util.List<String> gateway = org.spockframework.runtime.SpecInternals.MockImpl(this, 'gateway', java.util.List)
    apackage.ASpec$OrderFixtures fixtures = new apackage.ASpec$OrderFixtures()
    this.getSpecificationContext().getMockController().enterScope()
    fixtures.expectCharge(this, gateway)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 1)
    gateway.add('charge')
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 1)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 2)
    this.getSpecificationContext().getMockController().leaveScope()
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 2)
    this.getSpecificationContext().getMockController().leaveScope()
}

@spock.lang.Interactions
public void expectCharge(java.util.List<String> gateway) {
    throw new org.spockframework.runtime.InvalidSpecException('Method \'expectCharge\' is annotated with @Interactions and can only declare interactions when called from a Specification with a strongly-typed receiver, or when its explicit-spec overload (with a leading Specification argument) is called directly.')
}

@spock.lang.Interactions
public void expectCharge(spock.lang.Specification $spec, java.util.List<String> gateway) {
    org.spockframework.util.Checks.notNull($spec, 'Cannot declare mock interactions: the Specification passed to this @Interactions method is null.')
    $spec.getSpecificationContext().getMockController().addInteraction(new org.spockframework.mock.runtime.InteractionBuilder(4, 5, '1 * gateway.add(\"charge\")').setFixedCount(1).addEqualTarget(gateway).addEqualMethodName('add').setArgListKind(true, false).addEqualArg('charge').build())
}
/*--------- end::snapshot[] ---------*/
}
