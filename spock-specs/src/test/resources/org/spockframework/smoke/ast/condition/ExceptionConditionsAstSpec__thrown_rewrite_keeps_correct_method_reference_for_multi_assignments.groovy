package aPackage
import spock.lang.*

class ASpec extends Specification {
/*--------- tag::snapshot[] ---------*/
public java.lang.Object foobar() {
    throw new java.lang.IllegalStateException('foo')
}

public void $spock_feature_0_0() {
    def (java.lang.Object foobar, java.lang.Object b) = [null, null]
    this.getSpecificationContext().setThrownException(null)
    try {
        (foobar, b) = this.foobar()
    }
    catch (java.lang.Throwable $spock_ex) {
        this.getSpecificationContext().setThrownException($spock_ex)
    }
    finally {
    }
    this.thrownImpl(null, null, java.lang.IllegalStateException)
    this.getSpecificationContext().getMockController().leaveScope()
}
/*--------- end::snapshot[] ---------*/
}