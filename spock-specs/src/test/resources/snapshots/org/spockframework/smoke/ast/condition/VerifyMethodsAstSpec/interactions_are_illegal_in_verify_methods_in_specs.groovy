package aPackage
import spock.lang.*

class ASpec extends Specification {
/*--------- tag::snapshot[] ---------*/
Unable to produce AST for this phase due to earlier compilation error:
startup failed:
script.groovy: 3: Interactions are not allowed in 'verify' blocks. Put them before the 'verify' block or into a 'then' block. @ line 3, column 5.
       1 * Mock(Object).toString()
       ^

1 error
/*--------- end::snapshot[] ---------*/
}
