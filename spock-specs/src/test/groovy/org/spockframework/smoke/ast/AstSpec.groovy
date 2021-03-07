package org.spockframework.smoke.ast

import org.spockframework.EmbeddedSpecification
import spock.util.Show

class AstSpec extends EmbeddedSpecification {
  def "astToSourceFeatureBody renders only methods and its annotation by default"() {
    when:
    def result = compiler.astToSourceFeatureBody('''
    given:
    def nothing = null
    ''')

    then:
    result == '''\
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [org.spockframework.runtime.model.BlockKind.SETUP[]], parameterNames = [])
    public void $spock_feature_0_0() {
        java.lang.Object nothing = null
        this.getSpecificationContext().getMockController().leaveScope()
    }'''
  }


  def "astToSourceSpecBody renders only methods, fields, properties, object initializers and their annotation by default"() {
    when:
    def result = compiler.astToSourceSpecBody('''
    def foo = 'bar'

    def 'a feature'() {
        given:
        def nothing = null
    }
    ''')

    then:
    result == '''\
@org.spockframework.runtime.model.FieldMetadata(name = 'foo', ordinal = 0, line = 1, initializer = true)
    private java.lang.Object foo

    private java.lang.Object $spock_initializeFields() {
        foo = 'bar\'
    }

    @org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 3, blocks = [org.spockframework.runtime.model.BlockKind.SETUP[]], parameterNames = [])
    public void $spock_feature_0_0() {
        java.lang.Object nothing = null
        this.getSpecificationContext().getMockController().leaveScope()
    }'''
  }


  def "astToSourceFeatureBody can render everything"() {
    when:
    def result = compiler.astToSourceFeatureBody('''
    given:
    def nothing = null
    ''', Show.all())

    then:
    result == '''\
package apackage

import spock.lang.*

@org.spockframework.runtime.model.SpecMetadata(filename = 'scriptXXXXX.groovy', line = 1)
public class apackage.ASpec extends spock.lang.Specification {

    @org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [org.spockframework.runtime.model.BlockKind.SETUP[]], parameterNames = [])
    public void $spock_feature_0_0() {
        java.lang.Object nothing = null
        this.getSpecificationContext().getMockController().leaveScope()
    }

}'''
  }
}
