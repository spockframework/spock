package org.spockframework.smoke.ast

import org.spockframework.EmbeddedSpecification

class AstSpec extends EmbeddedSpecification {
  def "compare AST"() {
    when:
    def result = compiler.astToSourceFeatureBody('''
    when:
    new ArrayList<>(-1)

    then:
    Exception e = thrown()
    ''')

    then:
    result == '''\
package apackage

import spock.lang.*

@org.spockframework.runtime.model.SpecMetadata(filename = 'scriptXXXXX.groovy', line = 1)
public class apackage.ASpec extends spock.lang.Specification {

    @org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [org.spockframework.runtime.model.BlockKind.WHEN[]org.codehaus.groovy.ast.AnnotationNode@XXXXX, org.spockframework.runtime.model.BlockKind.THEN[]org.codehaus.groovy.ast.AnnotationNode@XXXXX], parameterNames = [])
    public void $spock_feature_0_0() {
        this.getSpecificationContext().setThrownException(null)
        try {
            new java.util.ArrayList(-1)
        }
        catch (java.lang.Throwable $spock_ex) {
            this.getSpecificationContext().setThrownException($spock_ex)
        }
        finally {
        }
        java.lang.Exception e = this.thrownImpl('e', java.lang.Exception)
        this.getSpecificationContext().getMockController().leaveScope()
    }

}'''
  }
}
