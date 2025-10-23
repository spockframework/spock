@apackage.another.Ann
package apackage.another

import static java.util.Collections.emptyList
import static java.nio.charset.StandardCharsets.*

@apackage.another.Ann
import java.text.ParseException
import java.nio.file.*

public abstract class apackage.another.Foo extends java.lang.Object implements java.lang.Comparable {

    private java.util.List<String> x = new java.util.ArrayList<>()

    public apackage.another.Foo(java.lang.String initialValue) {
        x << initialValue
    }

}
@apackage.another.Ann
package apackage.another

import static java.util.Collections.emptyList
import static java.nio.charset.StandardCharsets.*

@apackage.another.Ann
import java.text.ParseException
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
import java.text.ParseException
import java.nio.file.*

public abstract interface apackage.another.Ann extends java.lang.Object implements java.lang.annotation.Annotation {

}
@apackage.another.Ann
package apackage.another

import static java.util.Collections.emptyList
import static java.nio.charset.StandardCharsets.*

@apackage.another.Ann
import java.text.ParseException
import java.nio.file.*

@apackage.another.Ann
public class apackage.another.Bar extends apackage.another.Foo implements apackage.another.Ex, java.io.Serializable {

    private static final int[] ARR = (([1, 2, 3]) as int[])
    private static final java.lang.String STR = 'str'
    private static final char CHR = ' '
    private static final int INT = 10
    private static int st
    private int order
    private java.lang.String someString = "String with ${STR}"
    private java.lang.String plainString = 'plain'

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
        java.lang.Object g = "a: ${a} and b: ${ e.a.compareTo(c)}"
        java.lang.Integer h = 1
        java.lang.Integer i = 2
        java.lang.Object( j , k ) = x
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
        java.lang.Object w = { ->
            this.println(g)
        }
        java.lang.Object x = (( c ) as long)
        java.lang.Object y = ((long) c )
        java.lang.Object z = [1, 2, 3] [ 0]
        assert c == d : null
    }

    public void gstrings() {
        java.lang.Object a = 'simple'
        java.lang.Object b = 'normal string \$a ...'
        java.lang.Object c = "gstring ${a} ..."
        java.lang.Object d = "gstring with brackets ${a.size()} ..."
        java.lang.Object e = "gstring with closure ${ ->
            a
        } ... "
        java.lang.Object f = "with writer ${ java.lang.Object w ->
            w << a
        }"
        java.lang.Object g = 'simple\nmulti\nline'
        java.lang.Object h = "multi line gstring\n${a}\n..."
        java.lang.Object i = "multi line gstring\n with brackets ${a.size()}\n with escaped brackets \${a.size()}\n ..."
        java.lang.Object j = "multi line gstring\n with closure ${ ->
            a
        }\n with escaped closure \${-> a}\n ... "
        java.lang.Object k = "multi line gstring\n with simple value ${a}\n with simple escaped value \$a\n with brackets ${a.size()}\n with escaped brackets \${a.size()}\n with closure ${ ->
            a
        }\n with escaped closure \${-> a}\n with writer ${ java.lang.Object w ->
            w << a
        }\n with writer escaped \${w -> w << a}\n..."
    }

    @java.lang.Override
    public int compareTo(java.lang.Object o) {
        synchronized ( o ) {
            return order <=> (( o ) as apackage.another.Bar).order
        }
    }

    public void multix(java.nio.file.Path a, @apackage.another.Ann int b, java.lang.String desc = '') {
    }

    public void prop(java.util.List l, int[] a) {
        java.lang.Object x = l*.foo
        java.lang.Object y = a?.length
        java.lang.Object z = a."${STR}"
    }

    public static final void statMethod(java.lang.String a) {
    }

}
@apackage.another.Ann
package apackage.another

import static java.util.Collections.emptyList
import static java.nio.charset.StandardCharsets.*

@apackage.another.Ann
import java.text.ParseException
import java.nio.file.*

public class apackage.another.Ext<T extends java.io.Serializable, V extends java.lang.Cloneable> extends java.lang.Object {

    private T[] arr
    private java.util.List<V> lst = []

    public V foo(java.util.List<? super T> consumer) {
    }

    @apackage.another.Ann
    public <X extends java.io.Serializable & java.lang.Comparable<T>> boolean saveCompare(X a, X b) {
    }

}