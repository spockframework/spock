/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.smoke.ast

import org.codehaus.groovy.control.CompilePhase
import org.spockframework.EmbeddedSpecification
import org.spockframework.runtime.GroovyRuntimeUtil
import org.spockframework.specs.extension.SpockSnapshotter
import org.spockframework.util.GroovyReleaseInfo
import org.spockframework.util.VersionNumber
import spock.lang.Requires
import spock.lang.Snapshot
import spock.lang.Snapshotter
import spock.util.Show

class AstSpec extends EmbeddedSpecification {
  @Snapshot(extension = 'groovy')
  SpockSnapshotter snapshotter

  @Snapshot
  Snapshotter textSnapshotter

  def "astToSourceFeatureBody renders only methods and its annotation by default"() {
    given:
    snapshotter.featureBody()

    when:
    def result = compiler.transpileFeatureBody('''
    given:
    def nothing = null
    ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }


  def "astToSourceSpecBody renders only methods, fields, properties, object initializers and their annotation by default"() {
    given:
    snapshotter.specBody()

    when:
    def result = compiler.transpileSpecBody('''
    def foo = 'bar'

    def 'a feature'() {
        given:
        def nothing = null
    }
    ''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }



  @Requires({ GroovyReleaseInfo.version < VersionNumber.parse("4.0.2")})
  def "astToSourceFeatureBody can render everything"() {
    when:
    def result = compiler.transpileFeatureBody('''
    given:
    def nothing = null
    ''', Show.all(), CompilePhase.INSTRUCTION_SELECTION)

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  @Requires({ GroovyReleaseInfo.version >= VersionNumber.parse("4.0.2")})
  def "astToSourceFeatureBody can render everything (Groovy 4.0.2+)"() {
    when:
    def result = compiler.transpileFeatureBody('''
    given:
    def nothing = null
    ''', Show.all(), CompilePhase.INSTRUCTION_SELECTION)

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "astToSourceFeatureBody shows compile error in source"() {
    when:
    def result = compiler.transpileFeatureBody('''
    when:
    foo()
    expect: 'none'
    ''', Show.all(), CompilePhase.INSTRUCTION_SELECTION)

    then:
    textSnapshotter.assertThat(result.source.normalize()).matchesSnapshot()
  }

  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION >= 3 })
  def "groovy 3 language features"() {
    when:
    def result = compiler.transpile('''
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
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "enums"() {
    given:
    // groovy 4 renders differently
    def snapshotId = (GroovyRuntimeUtil.MAJOR_VERSION >= 4) ? "groovy4" : ""

    when:
    def result = compiler.transpile('''
    enum Alpha {
      A, B, C;
    }
''', Show.all().tap {
      it.remove(Show.ANNOTATIONS) // hide annotations as groovy 3 adds@groovy.transform.Generated, which is irrelevant for us
    })

    then:
    snapshotter.assertThat(result.source).matchesSnapshot(snapshotId)
  }

  def "full feature exercise"() {
    when:
    def result = compiler.transpile('''
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
  String someString = "String with $STR"
  String plainString = 'plain'

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
    def w = { println g }
    def x = c as long
    def y = (long)c
    def z = [1, 2, 3][0]
    assert c == d
  }

  void gstrings() {
    def a = "simple"
    def b = "normal string \\$a ..."
    def c = "gstring $a ..."
    def d = "gstring with brackets ${a.size()} ..."
    def e = "gstring with closure ${-> a} ... "
    def f = "with writer ${w -> w << a}"
    def g = """simple
multi
line"""
    def h =  """multi line gstring
$a
..."""
    def i = """multi line gstring
 with brackets ${a.size()}
 with escaped brackets \\${a.size()}
 ..."""
    def j = """multi line gstring
 with closure ${-> a}
 with escaped closure \\${-> a}
 ... """
    def k = """multi line gstring
 with simple value $a
 with simple escaped value \\$a
 with brackets ${a.size()}
 with escaped brackets \\${a.size()}
 with closure ${-> a}
 with escaped closure \\${-> a}
 with writer ${w -> w << a}
 with writer escaped \\${w -> w << a}
..."""
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

  static final void statMethod(String a) {}
}


class Ext <T extends Serializable, V extends Cloneable> {
  T[] arr
  List<V> lst = []

  V foo (List<? super T> consumer){}
  @Ann <X extends Serializable & Comparable<T>> boolean saveCompare(X a, X b){}
}
''')

    then:
    snapshotter.assertThat(result.source).matchesSnapshot()
  }

  def "Primitive types are used in AST transformation"() {
    given:
    def snapshotId = (GroovyRuntimeUtil.MAJOR_VERSION >= 4) ? "groovy4" : ""

    when:
    def result = compiler.transpileWithImports('''
class TestSpec extends Specification {
  def 'test'() {
    expect:
    true
    when:
    true
    then:
    thrown(RuntimeException)
  }
}
''',
        EnumSet.of(Show.METHODS),
        CompilePhase.OUTPUT)

    then:
    textSnapshotter.assertThat(result.source).matchesSnapshot(snapshotId)
  }
}
