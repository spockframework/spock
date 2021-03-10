package org.spockframework.smoke.ast

import org.spockframework.EmbeddedSpecification
import spock.util.Show

import org.codehaus.groovy.control.CompilePhase

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
    foo = 'bar'
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
    ''', Show.all(), CompilePhase.INSTRUCTION_SELECTION)

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

  def "full feature exercise"() {
    when:
    def result = compiler.astToSource('''
    package apackage

    import groovy.transform.EqualsAndHashCode

    abstract class Foo implements Comparable {
      List<String> x = new ArrayList<>()


    }

    @EqualsAndHashCode
    class Bar extends Foo {
      static final int[] ARR = [1, 2, 3] as int[]
      static final String STR = 'str'
      static final char CHR = ' '
      static final int INT = 10
      static int st
      int order

      Bar() {
       println '42'
      }

      static {
        st = 42
      }

      {
        order = 1
      }

      void method() {
        outer:
        for (int i = 0; i < INT; i++) {
          if(i % 2 == 0) {
            print 'even'
            continue outer
          } else {
            print 'odd'
          }
          break outer
        }
        for(String y : x) {
          print(y)
        }
      }

      @Override
      int compareTo(Object o){
        return order <=> (o as Bar).order
      }
    }

    class Ext <T extends Serializable> {
      def foo (List<? super T> consumer){}
    }
    ''')

    then:
    result == '''\
package apackage

import groovy.transform.EqualsAndHashCode as EqualsAndHashCode

public abstract class apackage.Foo extends java.lang.Object implements java.lang.Comparable {

    private java.util.List<String> x

}
package apackage

import groovy.transform.EqualsAndHashCode as EqualsAndHashCode

@groovy.transform.EqualsAndHashCode
public class apackage.Bar extends apackage.Foo {

    private static final [I ARR
    private static final java.lang.String STR = 'str'
    private static final char CHR = ' '
    private static final int INT = 10
    private static int st
    private int order

    public apackage.Bar() {
        this.println('42')
    }

    {
        order = 1
    }

    static {
        st = 42
    }

    public void method() {
        outer:
        for (java.lang.Integer i = 0; i < INT ;( i )++) {
            if ( i % 2 == 0) {
                this.print('even')
                continue outer
            } else {
                this.print('odd')
            }
            break outer
        }
        for (java.lang.String y : x ) {
            this.print(y)
        }
    }

    @java.lang.Override
    public int compareTo(java.lang.Object o) {
        return order <=> (( o ) as apackage.Bar).order
    }

}
package apackage

import groovy.transform.EqualsAndHashCode as EqualsAndHashCode

public class apackage.Ext<T extends java.io.Serializable> extends java.lang.Object {

    public java.lang.Object foo(java.util.List<? super java.io.Serializable<T extends java.io.Serializable>> consumer) {
    }

}'''

  }
}
