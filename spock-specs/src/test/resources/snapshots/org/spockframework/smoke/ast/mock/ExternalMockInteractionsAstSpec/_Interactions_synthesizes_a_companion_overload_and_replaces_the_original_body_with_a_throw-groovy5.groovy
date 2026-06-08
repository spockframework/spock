import spock.lang.Interactions

public class OrderFixtures extends java.lang.Object {

    @spock.lang.Interactions
    public void stubHappyPath(java.util.List<String> gateway) {
        throw new org.spockframework.runtime.InvalidSpecException('Method \'stubHappyPath\' is annotated with @Interactions and can only declare interactions when called from a Specification with a strongly-typed receiver, or when its explicit-spec overload (with a leading Specification argument) is called directly.')
    }

    @spock.lang.Interactions
    public void stubHappyPath(spock.lang.Specification $spec, java.util.List<String> gateway) {
        org.spockframework.util.Checks.notNull($spec, 'Cannot declare mock interactions: the Specification passed to this @Interactions method is null.')
        $spec.getSpecificationContext().getMockController().addInteraction(new org.spockframework.mock.runtime.InteractionBuilder(7, 5, 'gateway.add(\"x\") >> true').addEqualTarget(gateway).addEqualMethodName('add').setArgListKind(true, false).addEqualArg('x').addConstantResponse(true).build())
        $spec.getSpecificationContext().getMockController().addInteraction(new org.spockframework.mock.runtime.InteractionBuilder(8, 5, '1 * gateway.add(\"y\")').setFixedCount(1).addEqualTarget(gateway).addEqualMethodName('add').setArgListKind(true, false).addEqualArg('y').build())
    }

}