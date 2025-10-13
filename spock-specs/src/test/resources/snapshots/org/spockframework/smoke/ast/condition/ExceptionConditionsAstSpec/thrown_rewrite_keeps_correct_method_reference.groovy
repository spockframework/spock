package aPackage
import spock.lang.*

class ASpec extends Specification {
/*--------- tag::snapshot[] ---------*/
public java.lang.Object foobar() {
    throw new java.lang.IllegalStateException('foo')
}

public void $spock_feature_0_0() {
    java.lang.Object foobar
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 0)
    this.getSpecificationContext().setThrownException(null)
    try {
        foobar = this.foobar()
    }
    catch (java.lang.Throwable $spock_ex) {
        this.getSpecificationContext().setThrownException($spock_ex)
    }
    finally {
    }
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 0)
    org.spockframework.runtime.SpockRuntime.callBlockEntered(this, 1)
    org.spockframework.runtime.SpecialMethodCallTarget.thrownImpl(this, null, null, java.lang.IllegalStateException)
    org.spockframework.runtime.SpockRuntime.callBlockExited(this, 1)
    this.getSpecificationContext().getMockController().leaveScope()
}
/*--------- end::snapshot[] ---------*/
}
