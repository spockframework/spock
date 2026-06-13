import groovy.transform.SelfType
import spock.lang.Specification

@groovy.transform.Trait
@groovy.transform.SelfType(value = spock.lang.Specification)
public abstract interface OrderInteractions extends java.lang.Object {

    @org.codehaus.groovy.transform.trait.Traits$Implemented
    public abstract java.lang.Object createAndStub() {
    }

}
import groovy.transform.SelfType
import spock.lang.Specification

@groovy.transform.SelfType(value = spock.lang.Specification)
@groovy.transform.Generated
public abstract static class OrderInteractions$Trait$Helper extends java.lang.Object {

    public static void $init$(OrderInteractions $self) {
    }

    public static void $static$init$(java.lang.Class<OrderInteractions> $static$self) {
    }

    public static java.lang.Object createAndStub(OrderInteractions $self) {
        java.lang.Object gateway = org.spockframework.runtime.SpecInternals.MockImpl($self, 'gateway', null, java.util.List)
        $self.getSpecificationContext().getMockController().addInteraction(new org.spockframework.mock.runtime.InteractionBuilder(9, 5, 'gateway.add(\"x\") >> true').addEqualTarget(gateway).addEqualMethodName('add').setArgListKind(true, false).addEqualArg('x').addConstantResponse(true).build())
        $self.getSpecificationContext().getMockController().addInteraction(new org.spockframework.mock.runtime.InteractionBuilder(10, 5, '1 * gateway.add(\"y\")').setFixedCount(1).addEqualTarget(gateway).addEqualMethodName('add').setArgListKind(true, false).addEqualArg('y').build())
        return gateway
    }

}