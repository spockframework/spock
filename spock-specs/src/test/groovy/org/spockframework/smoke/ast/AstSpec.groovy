package org.spockframework.smoke.ast

import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.GroovyRuntimeUtil
import spock.lang.Requires
import spock.util.Show

import org.codehaus.groovy.control.CompilePhase

class AstSpec extends EmbeddedSpecification {
  def "astToSourceFeatureBody renders only methods and its annotation by default"() {
    when:
    def result = compiler.transpileToAstFeatureBody('''
    given:
    def nothing = null
    ''')

    then:
    result.source == '''\
@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [org.spockframework.runtime.model.BlockKind.SETUP[]@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    java.lang.Object nothing = null
    this.getSpecificationContext().getMockController().leaveScope()
}'''
  }


  def "astToSourceSpecBody renders only methods, fields, properties, object initializers and their annotation by default"() {
    when:
    def result = compiler.transpileToAstSpecBody('''
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

@org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 3, blocks = [org.spockframework.runtime.model.BlockKind.SETUP[]@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = [])], parameterNames = [])
public void $spock_feature_0_0() {
    java.lang.Object nothing = null
    this.getSpecificationContext().getMockController().leaveScope()
}'''
  }


  def "astToSourceFeatureBody can render everything"() {
    when:
    def result = compiler.transpileToAstFeatureBody('''
    given:
    def nothing = null
    ''', Show.all(), CompilePhase.INSTRUCTION_SELECTION)

    then:
    result.source == '''\
package apackage

import spock.lang.*

@org.spockframework.runtime.model.SpecMetadata(filename = 'script.groovy', line = 1)
public class apackage.ASpec extends spock.lang.Specification {

    @org.spockframework.runtime.model.FeatureMetadata(name = 'a feature', ordinal = 0, line = 1, blocks = [org.spockframework.runtime.model.BlockKind.SETUP[]@org.spockframework.runtime.model.BlockMetadata(kind = org.spockframework.runtime.model.BlockKind.SETUP, texts = [])], parameterNames = [])
    public void $spock_feature_0_0() {
        java.lang.Object nothing = null
        this.getSpecificationContext().getMockController().leaveScope()
    }

}'''
  }
  def "astToSourceFeatureBody shows compile error in source"() {
    when:
    def result = compiler.transpileToAstFeatureBody('''
    when:
    foo()
    expect: 'none'
    ''', Show.all(), CompilePhase.INSTRUCTION_SELECTION)

    then:
    result.source.normalize() == '''\
Unable to produce AST for this phase due to earlier compilation error:
startup failed:
script.groovy: 3: 'expect' is not allowed here; instead, use one of: [and, then] @ line 3, column 13.
       expect: 'none'
               ^

1 error'''
  }

  @Requires({ GroovyRuntimeUtil.groovy3orNewer })
  def "groovy 3 language features"() {
    when:
    def result = compiler.transpileToAst('''
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
    def result = compiler.transpileToAst('''
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
    def result = compiler.transpileToAst('''
@Ann
package apackage.another

import static java.util.Collections.emptyList
import static java.nio.charset.StandardCharsets.*

import java.nio.file.*
@Ann
import java.text.ParseException


abstract class Foo implements Comparable {
  List<String> x = new ArrayList<>()
  Foo(String initialValue) {
    x << initialValue
  }
}

interface Ex {
  void ex() throws IOException, ParseException;
}

@interface Ann {}


@Ann
class Bar extends Foo implements Ex, Serializable {
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
    def s = { x -> x*x }
    def t = [:]
    def u = ++c
    def v = "$STR"(a)
    assert c == d
  }

  @Override
  int compareTo(Object o) {
    synchronized (o) {
      return order <=> (o as Bar).order
    }
  }

  void multix(Path a, @Ann int b, String desc = ''){}

  void prop(List l, int[] a) {
    def x = l*.foo
    def y = a?.length
    def z = a."$STR"
  }
}


class Ext <T extends Serializable, V extends Cloneable> {
  V foo (List<? super T> consumer){}
  @Ann <X extends Serializable & Comparable<T>> boolean saveCompare(X a, X b){}
}
''')

    then:
    result.source == '''\
@apackage.another.Ann
package apackage.another

import static java.util.Collections.emptyList
import static java.nio.charset.StandardCharsets.*

@apackage.another.Ann
import java.text.ParseException as ParseException
import java.nio.file.*

public abstract class apackage.another.Foo extends java.lang.Object implements java.lang.Comparable {

    private java.util.List<String> x

    public apackage.another.Foo(java.lang.String initialValue) {
        x << initialValue
    }

}
@apackage.another.Ann
package apackage.another

import static java.util.Collections.emptyList
import static java.nio.charset.StandardCharsets.*

@apackage.another.Ann
import java.text.ParseException as ParseException
import java.nio.file.*

public abstract interface apackage.another.Ex extends java.lang.Object {

    public abstract void ex() throws java.io.IOException, java.text.ParseException {
    }

}
@apackage.another.Ann
package apackage.another

import static java.util.Collections.emptyList
import static java.nio.charset.StandardCharsets.*

@apackage.another.Ann
import java.text.ParseException as ParseException
import java.nio.file.*

public abstract interface apackage.another.Ann extends java.lang.Object implements java.lang.annotation.Annotation {

}
@apackage.another.Ann
package apackage.another

import static java.util.Collections.emptyList
import static java.nio.charset.StandardCharsets.*

@apackage.another.Ann
import java.text.ParseException as ParseException
import java.nio.file.*

@apackage.another.Ann
public class apackage.another.Bar extends apackage.another.Foo implements apackage.another.Ex, java.io.Serializable {

    private static final [I ARR
    private static final java.lang.String STR = 'str'
    private static final char CHR = ' '
    private static final int INT = 10
    private static int st
    private int order

    public apackage.another.Bar() {
        super(apackage.another.Bar.STR)
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
        java.lang.Object g = "a: ${a} and b: ${e.a.compareTo(c)}"
        java.lang.Integer h = 1
        java.lang.Integer i = 2
        def (java.lang.Object j, java.lang.Object k) = x
        java.lang.Object l = b ? c : d
        java.lang.Object m = c ? c : d
        java.lang.Object n = this.&'convert'
        java.lang.Object o = this.order
        java.lang.Object p = (1..5)
        java.lang.Object q = (1..<5)
        java.lang.Object r = x*.size()?.intersect([1])
        java.lang.Object s = { java.lang.Object x ->
            x * x
        }
        java.lang.Object t = [:]
        java.lang.Object u = ++( c )
        java.lang.Object v = this."${STR}"(a)
        assert c == d : null
    }

    @java.lang.Override
    public int compareTo(java.lang.Object o) {
        synchronized ( o ) {
            return order <=> (( o ) as apackage.another.Bar).order
        }
    }

    public void multix(java.nio.file.Path a, @apackage.another.Ann int b, java.lang.String desc = '') {
    }

    public void prop(java.util.List l, [I a) {
        java.lang.Object x = l*.foo
        java.lang.Object y = a?.length
        java.lang.Object z = a."${STR}"
    }

}
@apackage.another.Ann
package apackage.another

import static java.util.Collections.emptyList
import static java.nio.charset.StandardCharsets.*

@apackage.another.Ann
import java.text.ParseException as ParseException
import java.nio.file.*

public class apackage.another.Ext<T extends java.io.Serializable, V extends java.lang.Cloneable> extends java.lang.Object {

    public java.lang.Cloneable<V extends java.lang.Cloneable> foo(java.util.List<? super java.io.Serializable<T extends java.io.Serializable>> consumer) {
    }

    @apackage.another.Ann
    public boolean saveCompare(java.io.Serializable<X extends java.io.Serializable & java.lang.Comparable<T>> a, java.io.Serializable<X extends java.io.Serializable & java.lang.Comparable<T>> b) {
    }

}'''

  }
}
