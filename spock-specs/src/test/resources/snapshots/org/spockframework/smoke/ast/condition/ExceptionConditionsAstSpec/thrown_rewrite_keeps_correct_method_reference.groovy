package aPackage
import spock.lang.*

class ASpec extends Specification {
/*--------- tag::snapshot[] ---------*/
public java.lang.Object foobar() {
    throw new java.lang.IllegalStateException('foo')
}

public void $spock_feature_0_0() {
    try {
        java.lang.Object foobar
        this.getSpecificationContext().setThrownException(null)
        try {
            org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.WHEN, []))
            foobar = this.foobar()
            org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.WHEN, []))
        }
        catch (java.lang.Throwable $spock_ex) {
            this.getSpecificationContext().setThrownException($spock_ex)
        }
        finally {
        }
        org.spockframework.runtime.SpockRuntime.callEnterBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.THEN, []))
        this.thrownImpl(null, null, java.lang.IllegalStateException)
        org.spockframework.runtime.SpockRuntime.callExitBlock(this.getSpecificationContext(), new org.spockframework.runtime.model.BlockInfo(org.spockframework.runtime.model.BlockKind.THEN, []))
        this.getSpecificationContext().getMockController().leaveScope()
    }
    finally {
        org.spockframework.runtime.SpockRuntime.clearCurrentBlock(this.getSpecificationContext())}
}
/*--------- end::snapshot[] ---------*/
}
