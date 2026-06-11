/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.parameterization

import groovy.lang.Tuple1
import groovy.lang.Tuple2
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.spockframework.EmbeddedSpecification
import org.spockframework.compiler.InvalidSpecCompileException
import org.spockframework.runtime.GroovyRuntimeUtil
import spock.lang.Requires

import java.lang.reflect.ParameterizedType

class StandaloneDataProviderMethods extends EmbeddedSpecification {

  private Class compilePlainClass(String source) {
    new GroovyClassLoader(getClass().classLoader).parseClass("import spock.lang.DataProvider\n\n" + source)
  }

  def "data table provider in a spec is consumed via multi-variable data pipe"() {
    when:
    def result = runner.runSpecBody '''
@DataProvider
def pairs() {
  a | b
  1 | 2
  2 | 3
}

def feature() {
  expect:
  b == a + 1

  where:
  [a, b] << pairs()
}
'''

    then:
    result.testsSucceededCount == 3  // the feature node plus one node per iteration
  }

  def "single column provider uses Tuple1 and is consumed via single-element list pipe"() {
    when:
    def result = runner.runSpecBody '''
@DataProvider
def values() {
  a << [1, 2, 3]
}

def feature() {
  expect:
  a in [1, 2, 3]

  where:
  [a] << values()
}
'''

    then:
    result.testsSucceededCount == 4
  }

  def "rows are emitted as arity-specific tuples"() {
    given:
    def clazz = compiler.compileSpecBody '''
@DataProvider
def pairs() {
  a || b
  1 || 2
  2 || 3
}
'''

    when:
    def rows = clazz.newInstance().pairs().collect()

    then:
    rows.every { it instanceof Tuple2 }
    rows*.toList() == [[1, 2], [2, 3]]
  }

  def "single column rows are emitted as Tuple1"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def values() {
    a << [1, 2]
  }
}
'''

    when:
    def rows = clazz.newInstance().values().collect()

    then:
    rows.every { it instanceof Tuple1 }
    rows*.toList() == [[1], [2]]
  }

  def "provider supports the full where-block grammar"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def rich() {
    a | b
    1 | 10
    2 | 20
    3 | 30

    c = a + b

    filter:
    c < 33
  }
}
'''

    when:
    def rows = clazz.newInstance().rich().collect()

    then:
    rows*.toList() == [[1, 10, 11], [2, 20, 22]]
  }

  def "data table cells can reference previous columns"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def table() {
    x | y
    1 | x + 1
    2 | x + 2
  }
}
'''

    expect:
    clazz.newInstance().table().collect { it.toList() } == [[1, 2], [2, 4]]
  }

  def "combined data providers yield the cross product"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def matrix() {
    x << [1, 2]
    combined:
    y << ['a', 'b']
  }
}
'''

    when:
    def rows = clazz.newInstance().matrix().collect()

    then:
    rows*.toList() == [[1, 'a'], [1, 'b'], [2, 'a'], [2, 'b']]
  }

  def "provider can be a static method"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  static pairs() {
    a | b
    1 | 2
  }
}
'''

    expect:
    clazz.pairs().collect { it.toList() } == [[1, 2]]
  }

  def "static provider in a spec is consumed via data pipe"() {
    when:
    def result = runner.runSpecBody '''
@DataProvider
static pairs() {
  a | b
  1 | 2
  2 | 3
}

def feature() {
  expect:
  b == a + 1

  where:
  [a, b] << pairs()
}
'''

    then:
    result.testsSucceededCount == 3
  }

  def "instance provider on a plain class can access instance fields"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  def values = [1, 2, 3]

  @DataProvider
  def items() {
    a << values
  }
}
'''

    expect:
    clazz.newInstance().items().collect { it.toList() } == [[1], [2], [3]]
  }

  def "provider in a spec can access @Shared and static fields"() {
    when:
    def result = runner.runSpecBody '''
@Shared
List shared = [1, 2]

@DataProvider
def items() {
  a << shared
}

def feature() {
  expect:
  a in [1, 2]

  where:
  [a] << items()
}
'''

    then:
    result.testsSucceededCount == 3
  }

  def "provider in a spec must not access instance fields"() {
    when:
    compiler.compileSpecBody '''
List values = [1, 2]

@DataProvider
def items() {
  a << values
}
'''

    then:
    InvalidSpecCompileException e = thrown()
    e.message.startsWith("Only @Shared and static fields may be accessed from here")
  }

  def "each invocation returns a fresh iterator"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def values() {
    a << [1, 2]
  }
}
'''
    def instance = clazz.newInstance()

    expect:
    instance.values().collect { it.toList() } == [[1], [2]]
    instance.values().collect { it.toList() } == [[1], [2]]
  }

  def "method parameters are body-wide inputs but not columns"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  static upTo(int n) {
    final base = n * 10
    a << (1..n)
    b = a + base

    filter:
    b < 32
  }
}
'''

    expect:
    clazz.upTo(3).collect { it.toList() } == [[1, 31]]
    clazz.upTo(1).collect { it.toList() } == [[1, 11]]
  }

  def "method parameters are kept verbatim in the rewritten signature"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  static upTo(int n, String tag) {
    a << (1..n)
    b = tag + a
  }
}
'''

    when:
    def method = clazz.declaredMethods.find { it.name == "upTo" }

    then:
    method.parameterTypes*.simpleName == ["int", "String"]
    clazz.upTo(2, "x").collect { it.toList() } == [[1, "x1"], [2, "x2"]]
  }

  def "the estimated iteration count of the returned iterator is accurate for known sizes"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def known() {
    a << [1, 2, 3]
  }

  @DataProvider
  def unknown() {
    a << [1, 2, 3].iterator()
  }
}
'''
    def instance = clazz.newInstance()

    expect:
    instance.known().getEstimatedNumIterations() == 3
    instance.unknown().getEstimatedNumIterations() == -1
  }

  def "a consuming feature picks up the estimated iteration count"() {
    when:
    def result = runner.runSpecBody '''
@DataProvider
def pairs() {
  a | b
  1 | 2
  2 | 3
  3 | 4
}

def feature() {
  expect:
  specificationContext.currentIteration.estimatedNumIterations == 3

  where:
  [a, b] << pairs()
}
'''

    then:
    result.testsSucceededCount == 4
  }

  def "a consuming feature reports an unknown iteration count for a lazy data pipe"() {
    when:
    def result = runner.runSpecBody '''
@DataProvider
def lazy() {
  a << [1, 2, 3].iterator()
}

def feature() {
  expect:
  specificationContext.currentIteration.estimatedNumIterations == -1

  where:
  [a] << lazy()
}
'''

    then:
    result.testsSucceededCount == 4
  }

  def "where-block variables are visible to pipes and the filter block"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def items() {
    final limit = 3
    final threshold = 2
    a << (1..limit)

    filter:
    a >= threshold
  }
}
'''

    expect:
    clazz.newInstance().items().collect { it.toList() } == [[2], [3]]
  }

  def "the returned iterator exposes the data variable names"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def pairs() {
    a | b
    1 | 2
  }
}
'''

    expect:
    clazz.newInstance().pairs().getDataVariableNames() == ["a", "b"]
  }

  def "where-block variables are evaluated once and are not columns"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def items() {
    final marker = new Object()
    a << [1, 2]
    b = marker
  }
}
'''

    when:
    def rows = clazz.newInstance().items().collect()

    then:
    rows.size() == 2
    rows.every { it.size() == 2 }
    rows[0][1].is(rows[1][1])  // single evaluation
  }

  def "AutoCloseable where-block variables are closed in reverse order on close"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  static List closed = []

  @DataProvider
  def items() {
    final first = new Res(name: "first")
    final second = new Res(name: "second")
    a << [1, 2]
  }
}

class Res implements AutoCloseable {
  String name
  void close() { Providers.closed << name }
}
'''

    when:
    def iterator = clazz.newInstance().items()
    iterator.next()
    iterator.close()

    then:
    clazz.closed == ["second", "first"]
  }

  def "AutoCloseable where-block variables are closed on exhaustion"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  static List closed = []

  @DataProvider
  def items() {
    final res = new Res(name: "res")
    a << [1, 2]
  }
}

class Res implements AutoCloseable {
  String name
  void close() { Providers.closed << name }
}
'''

    when: "the iterator is exhausted and discarded without an explicit close()"
    def values = clazz.newInstance().items().collect { it[0] }

    then:
    values == [1, 2]
    clazz.closed == ["res"]
  }

  def "AutoCloseable where-block variables are closed when a data provider fails"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  static List closed = []

  @DataProvider
  def items() {
    final res = new Res(name: "res")
    a << broken()
  }

  static List broken() { throw new IllegalStateException("boom") }
}

class Res implements AutoCloseable {
  String name
  void close() { Providers.closed << name }
}
'''

    when:
    clazz.newInstance().items()

    then:
    IllegalStateException e = thrown()
    e.message == "boom"
    clazz.closed == ["res"]
  }

  def "already-created where-block variables are closed in reverse declaration order when a later initializer throws"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  static List closed = []

  @DataProvider
  def items() {
    final first = new Res(name: "first")
    final second = new Res(name: "second")
    final boom = broken()
    a << [1, 2]
  }

  static def broken() { throw new IllegalStateException("boom") }
}

class Res implements AutoCloseable {
  String name
  void close() { Providers.closed << name }
}
'''

    when:
    instantiate(clazz).items()

    then:
    IllegalStateException e = thrown()
    e.message == "boom"
    clazz.closed == ["second", "first"]
  }

  def "close is idempotent"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  static List closed = []

  @DataProvider
  def items() {
    final res = new Res(name: "res")
    a << [1]
  }
}

class Res implements AutoCloseable {
  String name
  void close() { Providers.closed << name }
}
'''

    when:
    def iterator = clazz.newInstance().items()
    iterator.collect()
    iterator.close()
    iterator.close()

    then:
    clazz.closed == ["res"]
  }

  @Requires(value = { GroovyRuntimeUtil.MAJOR_VERSION >= 3 }, reason = "'final (a, b) = ...' is only parseable by the Parrot parser (Groovy 3.0+)")
  def "multiple-assignment where-block variables are supported"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def items() {
    final (low, high) = [1, 3]
    a << (low..high)
  }
}
'''

    expect:
    clazz.newInstance().items().collect { it.toList() } == [[1], [2], [3]]
  }

  def "a def provider gets a synthesized Iterator return type"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def pairs() {
    a | b
    1 | 2
  }
}
'''

    when:
    def method = clazz.declaredMethods.find { it.name == "pairs" }

    then:
    method.returnType == Iterator
    method.genericReturnType instanceof ParameterizedType
    (method.genericReturnType as ParameterizedType).actualTypeArguments[0].typeName.contains("Tuple2")
  }

  def "explicit return type declarations are accepted when valid"() {
    when:
    compiler.compileSpecBody """
@DataProvider
$returnType pairs() {
  a | b
  1 | 2
}
"""

    then:
    noExceptionThrown()

    where:
    returnType << ["Iterator", "Iterator<Tuple2>", "Iterator<Tuple2<Integer, Integer>>"]
  }

  def "invalid return type declarations are compile errors"() {
    when:
    compiler.compileSpecBody """
@DataProvider
$returnType pairs() {
  a | b
  1 | 2
}
"""

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains(expectedMessagePart)

    where:
    returnType           | expectedMessagePart
    "List"               | "must be declared 'def' or with a return type of java.util.Iterator"
    "Iterable"           | "must be declared 'def' or with a return type of java.util.Iterator"
    "Iterator<Tuple3>"   | "declares return type Iterator<Tuple3>, but produces 2 data variable(s) [a, b]"
    "Iterator<String>"   | "element type must be groovy.lang.Tuple or an arity-specific Tuple class"
  }

  def "a body without any data variable is a compile error"() {
    when:
    compiler.compileSpecBody '''
@DataProvider
def empty() {
}
'''

    then:
    InvalidSpecCompileException e = thrown()
    e.message.contains("must declare at least one data variable")
  }

  def "a body with only where-block variables is a compile error"() {
    when:
    compiler.compileSpecBody '''
@DataProvider
def onlyLocals() {
  final x = 1
}
'''

    then:
    InvalidSpecCompileException e = thrown()
    e.message.startsWith("where-block variables require at least one data variable")
  }

  def "where-block variable rules are inherited"() {
    when:
    compiler.compileSpecBody """
@DataProvider
def items() {
$body
}
"""

    then:
    InvalidSpecCompileException e = thrown()
    e.message.startsWith(expectedMessagePart)

    where:
    body                                | expectedMessagePart
    'def sep = "/"\na << [1]'           | "where-block variables must be declared 'final'"
    'a << [1]\nfinal sep = "/"'         | "where-block variables must be declared at the beginning"
    'final $spock_x = 1\na << [1]'      | "Variable name '\$spock_x' is invalid"
    'final sep = "/"\nsep << [1]'       | "Data variable 'sep' collides with a where-block variable of the same name"
  }

  def "a data variable colliding with a method parameter is a compile error"() {
    when:
    compiler.compileSpecBody '''
@DataProvider
def upTo(int n) {
  n << [1]
}
'''

    then:
    InvalidSpecCompileException e = thrown()
    e.message.startsWith("Data variable 'n' collides with a method parameter of the same name")
  }

  def "a where-block variable colliding with a method parameter is a compile error"() {
    when:
    compiler.compileSpecBody '''
@DataProvider
def upTo(int n) {
  final n = 1
  a << [n]
}
'''

    then: "Groovy itself already rejects a local variable shadowing a parameter"
    org.codehaus.groovy.syntax.SyntaxException e = thrown()
    e.message.contains("variable") && e.message.contains("n")
  }

  def "@CompileStatic on the method is a compile error"() {
    when:
    compilePlainClass '''
import groovy.transform.CompileStatic

class Providers {
  @CompileStatic
  @DataProvider
  def pairs() {
    a | b
    1 | 2
  }
}
'''

    then:
    MultipleCompilationErrorsException e = thrown()
    e.message.contains("@DataProvider methods do not support @CompileStatic")
  }

  def "@CompileStatic on the declaring class is a compile error"() {
    when:
    compilePlainClass '''
import groovy.transform.CompileStatic

@CompileStatic
class Providers {
  @DataProvider
  def pairs() {
    a | b
    1 | 2
  }
}
'''

    then:
    MultipleCompilationErrorsException e = thrown()
    e.message.contains("@DataProvider methods do not support @CompileStatic")
  }

  def "rows above the highest fixed tuple arity fall back to the generic Tuple"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def wide() {
    a1 << [1]; a2 << [2]; a3 << [3]; a4 << [4]; a5 << [5]; a6 << [6]
    a7 << [7]; a8 << [8]; a9 << [9]; a10 << [10]
  }
}
'''

    when:
    def rows = clazz.newInstance().wide().collect()

    then:
    rows*.toList() == [[1, 2, 3, 4, 5, 6, 7, 8, 9, 10]]
    // Groovy 2.5 only ships Tuple1-Tuple9, so arity 10 degrades to the generic Tuple there
    rows[0].getClass().name == (GroovyRuntimeUtil.MAJOR_VERSION >= 3 ? "groovy.lang.Tuple10" : "groovy.lang.Tuple")
  }

  def "the rewritten method carries the standalone data provider metadata"() {
    given:
    def clazz = compilePlainClass '''
class Providers {
  @DataProvider
  def pairs() {
    a | b
    1 | 2
  }
}
'''

    when:
    def method = clazz.declaredMethods.find { it.name == "pairs" }
    def metadata = method.getAnnotation(org.spockframework.runtime.model.StandaloneDataProviderMetadata)

    then:
    metadata != null
    metadata.dataVariables() == ["a", "b"]
    metadata.line() > 0
  }

  def "provider consumed indirectly still works"() {
    when:
    def result = runner.runSpecBody '''
@DataProvider
def pairs() {
  a | b
  1 | 2
  2 | 3
}

def feature() {
  expect:
  x in [1, 2]

  where:
  x << pairs().collect { it[0] }
}
'''

    then:
    result.testsSucceededCount == 3
  }
}
