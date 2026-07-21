import spock.lang.Specification
import spock.mock.MockInteractionSupport

public class OrderFixtures extends java.lang.Object implements spock.mock.MockInteractionSupport {

    private final spock.lang.Specification specification

    public OrderFixtures(spock.lang.Specification specification) {
        this.specification = specification
    }

    public java.lang.Object createAndStub() {
        org.spockframework.util.Checks.notNull(this.getSpecification(), 'Cannot declare mock interactions: the owning Specification is null. Attach the MockInteractionSupport to a running Specification through a constructor field.')
        java.lang.Object gateway = org.spockframework.runtime.SpecInternals.MockImpl(this.getSpecification(), 'gateway', null, java.util.List)
        this.getSpecification().getSpecificationContext().getMockController().addInteraction(new org.spockframework.mock.runtime.InteractionBuilder(11, 5, 'gateway.add(\"x\") >> true').addEqualTarget(gateway).addEqualMethodName('add').setArgListKind(true, false).addEqualArg('x').addConstantResponse(true).build())
        this.getSpecification().getSpecificationContext().getMockController().addInteraction(new org.spockframework.mock.runtime.InteractionBuilder(12, 5, '1 * gateway.add(\"y\")').setFixedCount(1).addEqualTarget(gateway).addEqualMethodName('add').setArgListKind(true, false).addEqualArg('y').build())
        return gateway
    }

}