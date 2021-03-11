package org.spockframework.smoke.ast

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.GroovyRuntimeUtil
import spock.lang.Requires
import spock.util.Show

import org.codehaus.groovy.control.CompilePhase

class AstSpec extends EmbeddedSpecification {
  def "astToSourceFeatureBody renders only methods and its annotation by default"() {
    when:
    def result = compiler.compileToAstFeatureBody('''
    given:
    def nothing = null
    ''')

    then:
    result.source == '''\
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [org.spockframework.runtime.model.BlockKind.SETUP[]], parameterNames = [])
public void $spock_feature_0_0() {
    java.lang.Object nothing = null
    this.getSpecificationContext().getMockController().leaveScope()
}'''
  }


  def "astToSourceSpecBody renders only methods, fields, properties, object initializers and their annotation by default"() {
    when:
    def result = compiler.compileToAstSpecBody('''
    def foo = 'bar'

    def 'a feature'() {
        given:
        def nothing = null
    }
    ''')

    then:
    result.source == '''\
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
    def result = compiler.compileToAstFeatureBody('''
    given:
    def nothing = null
    ''', Show.all(), CompilePhase.INSTRUCTION_SELECTION)

    then:
    result.source == '''\
package apackage

import spock.lang.*

@org.spockframework.runtime.model.SpecMetadata(filename = 'script.groovy', line = 1)
public class apackage.ASpec extends spock.lang.Specification {

    @org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [org.spockframework.runtime.model.BlockKind.SETUP[]], parameterNames = [])
    public void $spock_feature_0_0() {
        java.lang.Object nothing = null
        this.getSpecificationContext().getMockController().leaveScope()
    }

}'''
  }

  @Requires({ GroovyRuntimeUtil.groovy3orNewer })
  def "groovy 3 language features"() {
    when:
    def result = compiler.compileToAst('''
class Foo {
  void loop() {
    do {
      println 'once'
    } while (false)
  }

  void methodRef() {
    [].forEach(System::print)
  }

  void lambdas() {
    def lambda = x -> x*x
    def lambdaMultiArg = (int a, int b) -> a <=> b
    def lambdaNoArg = () -> { throw new RuntimeException('bam') }
  }

  void blockStatements() {
    with_label:{
       println foo
    }
  }
}
''', EnumSet.of(Show.METHODS))
    then:
    result.source == '''\
public void loop() {
    do {
        this.println('once')
    } while (false)
}

public void methodRef() {
    [].forEach(java.lang.System::'print')
}

public void lambdas() {
    java.lang.Object lambda = ( java.lang.Object x) -> {
        x * x }
    java.lang.Object lambdaMultiArg = ( int a, int b) -> {
        a <=> b }
    java.lang.Object lambdaNoArg = ( ) -> {
        throw new java.lang.RuntimeException('bam')
    }
}

public void blockStatements() {
    with_label:
    {
        this.println(foo)
    }
}'''
  }

  def "enums"() {
    when:
    def result = compiler.compileToAst('''
    enum Alpha {
      A, B, C;
    }
''', Show.all().tap {
      it.remove(Show.ANNOTATIONS) // hide annotations as groovy 3 adds@groovy.transform.Generated, which is irrelevant for us
    })

    then:
    result.source == '''\
public final class Alpha extends java.lang.Enum<Alpha> {

    public static final Alpha A
    public static final Alpha B
    public static final Alpha C
    public static final Alpha MIN_VALUE
    public static final Alpha MAX_VALUE
    private static final Alpha[] $VALUES

    public static final Alpha[] values() {
        return $VALUES.clone()
    }

    public Alpha next() {
        java.lang.Object ordinal = this.ordinal().next()
        if ( ordinal >= $VALUES.size()) {
            ordinal = 0
        }
        return $VALUES.getAt( ordinal )
    }

    public Alpha previous() {
        java.lang.Object ordinal = this.ordinal().previous()
        if ( ordinal < 0) {
            ordinal = $VALUES.size().minus(1)
        }
        return $VALUES.getAt( ordinal )
    }

    public static Alpha valueOf(java.lang.String name) {
        return Alpha.valueOf(Alpha, name)
    }

    public static final Alpha $INIT(java.lang.Object[] para) {
        return this (* para )
    }

    static {
        A = Alpha.$INIT('A', 0)
        B = Alpha.$INIT('B', 1)
        C = Alpha.$INIT('C', 2)
        MIN_VALUE = A
        MAX_VALUE = C
        $VALUES = new Alpha[]{A, B, C}
    }

}'''
  }

  def "full feature exercise"() {
    when:
    def result = compiler.compileToAst('''
    package apackage

    import groovy.transform.EqualsAndHashCode

    abstract class Foo implements Comparable {
      List<String> x = new ArrayList<>()
      Foo(String initialValue) {
        x << initialValue
      }
    }

    interface Ex {
      void ex() throws Exception;
    }

    @interface Ann {}


    @Ann
    class Bar extends Foo implements Ex {
      static final int[] ARR = [1, 2, 3] as int[]
      static final String STR = 'str'
      static final char CHR = ' '
      static final int INT = 10
      static int st
      int order

      Bar() {
        super(STR)
      }

      static {
        st = 42
      }

      {
        order = 1
      }

      void loops() {
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
        while (true){
          break
        }
      }

      @Override
      void ex() throws Exception {
        try {
            System.getProperty("Foo", STR)?.stripIndent()
        } catch (RuntimeException e) {
          throw new Exception(e)
        } finally {
          println 'executed ex'
        }
      }

      String convert(String input) {
        String result = ''
        switch (input) {
        case 'Alpha.A':
          result = 'a'
          break
        case 'Alpha.B': // fallthrough
        case ~/Alpha.*/:
          result = 'c'
          break
        default:
          result = 'shrug'
        }
        return result
      }

      void operators(){
        int a = ~INT
        boolean b = !a
        int c = -a
        int d = +c
        def e = [a: b, (STR): c]
        def f = [*:e, foo: 'bar']
        def g = "a: $a and b: ${e.a.compareTo(c)}"
        int h = 1, i = 2
        def (j, k) = x
        def l = b ? c : d
        def m = c ?: d
        def n = this.&convert
        def o = this.@order
        def p = (1..5)
        def q = (1..<5)
        def r = x*.size()?.intersect([1])
        def s = { x-> x*x}
        assert c == d
      }

      @Override
      int compareTo(Object o) {
        synchronized (o) {
          return order <=> (o as Bar).order
        }
      }
    }

    class Ext <T extends Serializable> {
      def foo (List<? super T> consumer){}
    }

    ''')

    then:
    result.source == '''\
package apackage

import groovy.transform.EqualsAndHashCode as EqualsAndHashCode

public abstract class apackage.Foo extends java.lang.Object implements java.lang.Comparable {

    private java.util.List<String> x

    public apackage.Foo(java.lang.String initialValue) {
        x << initialValue
    }

}
package apackage

import groovy.transform.EqualsAndHashCode as EqualsAndHashCode

public abstract interface apackage.Ex extends java.lang.Object {

    public abstract void ex() throws java.lang.Exception {
    }

}
package apackage

import groovy.transform.EqualsAndHashCode as EqualsAndHashCode

public abstract interface apackage.Ann extends java.lang.Object implements java.lang.annotation.Annotation {

}
package apackage

import groovy.transform.EqualsAndHashCode as EqualsAndHashCode

@apackage.Ann
public class apackage.Bar extends apackage.Foo implements apackage.Ex {

    private static final [I ARR
    private static final java.lang.String STR = 'str'
    private static final char CHR = ' '
    private static final int INT = 10
    private static int st
    private int order

    public apackage.Bar() {
        super(apackage.Bar.STR)
    }

    {
        order = 1
    }

    static {
        st = 42
    }

    public void loops() {
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
        while (true) {
            break
        }
    }

    @java.lang.Override
    public void ex() throws java.lang.Exception {
        try {
            java.lang.System.getProperty('Foo', STR)?.stripIndent()
        }
        catch (java.lang.RuntimeException e) {
            throw new java.lang.Exception(e)
        }
        finally {
            this.println('executed ex')
        }
    }

    public java.lang.String convert(java.lang.String input) {
        java.lang.String result = ''
        switch ( input ) {
            case 'Alpha.A':
                result = 'a'
                break
            case 'Alpha.B':
            case ~('Alpha.*') :
                result = 'c'
                break
            default:
            result = 'shrug'
        }
        return result
    }

    public void operators() {
        java.lang.Integer a = ~( INT )
        java.lang.Boolean b = !( a )
        java.lang.Integer c = -( a )
        java.lang.Integer d = +( c )
        java.lang.Object e = ['a': b , STR : c ]
        java.lang.Object f = [*: e , 'foo': 'bar']
        java.lang.Object g = "a: $a and b: $e.a.compareTo(c)"
        java.lang.Integer h = 1
        java.lang.Integer i = 2
        def (java.lang.Object j, java.lang.Object k) = x
        java.lang.Object l = b ? c : d
        java.lang.Object m = c ? c : d
        java.lang.Object n = this .&'convert'
        java.lang.Object o = this .order
        java.lang.Object p = (1..5)
        java.lang.Object q = (1..<5)
        java.lang.Object r = x*.size()?.intersect([1])
        java.lang.Object s = { java.lang.Object x ->
            x * x
        }
        assert c == d : null
    }

    @java.lang.Override
    public int compareTo(java.lang.Object o) {
        synchronized ( o ) {
            return order <=> (( o ) as apackage.Bar).order
        }
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
