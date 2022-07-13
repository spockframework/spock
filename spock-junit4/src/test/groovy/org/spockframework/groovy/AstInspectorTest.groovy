/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.groovy

import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.stmt.ForStatement
import org.codehaus.groovy.control.CompilePhase
import org.junit.Before
import org.junit.Test
import org.spockframework.util.inspector.AstInspector
import org.spockframework.util.inspector.AstInspectorException
import org.spockframework.util.inspector.Inspect

import static org.junit.Assert.*

class AstInspectorTest {
  AstInspector inspector = new AstInspector(CompilePhase.SEMANTIC_ANALYSIS)

  @Before
  void loadSource() {
    inspector.load("""
import org.spockframework.util.inspector.Inspect

scriptLoop:
for (i in 1..5) println "hi!"
scriptExpr:
script(me)

@Inspect("bar1")
def bar() {}

class Foo {
  def field = inspect_init1(42)

  def Foo(@Inspect("param")f = inspect_init2(5)) {
    this.field = f
  }

  @Inspect("bar2")
  def bar() {
    stat1: def x = 10
    stat2:
    (1..inspect_upperValue(5)).each { println(3 * inspect_varRef(x)) }
    stat3: println "bye"
  }

  def inspect_init1(n) {}
  static inspect_init2(n) {}
}
    """)
  }

  @Test
  void inspectScript() {
    assertEquals(2, inspector.scriptStatements.size())
    assertEquals(1, inspector.scriptExpressions.size())

    def scriptLoop = inspector.getStatement("scriptLoop")
    assertTrue(scriptLoop instanceof ForStatement)

    def scriptStat = inspector.getStatement("scriptExpr")
    def scriptExpr = inspector.getExpression("scriptExpr")
    assertSame(scriptExpr, scriptStat.expression)
  }

  @Test
  void inspectModule() {
    // one class is auto-generated to hold the script
    assertEquals(2, inspector.module.classes.size())

    def scriptStats = inspector.module.statementBlock.statements
    assertEquals(scriptStats, inspector.scriptStatements)
  }

  @Test
  void inspectClass() {
    def foo = inspector.getClass("Foo")
    def field = inspector.getField("field")
    assertSame(field, foo.getField("field"))
  }

  @Test
  void inspectConstructor() {
    def ctor = inspector.getConstructor("Foo")
    assertTrue(inspector.getExpressions(ctor)[0] instanceof BinaryExpression)

    def param = inspector.getMarkedNode("param")
    def init1 = inspector.getExpression("init1")
    def init2 = inspector.getExpression("init2")

    assertEquals(42, init1.value)
    assertEquals(5, init2.value)
    assertSame(init2, param.initialExpression.arguments.expressions[0])
  }

  @Test
  void inspectMethod() {
    def method = inspector.getMarkedNode("bar2")
    def stat1 = inspector.getStatement("stat1")
    def stat2 = inspector.getStatement("stat2")
    def stat3 = inspector.getStatement("stat3")

    assertEquals([stat1,stat2,stat3],
      inspector.getStatements(method))

    def upperValue = inspector.getExpression("upperValue")
    assertEquals(5, upperValue.value)
  }

  @Test
  void inspectWhiskeyBars() {
    def bar = inspector.getMethod("bar")
    def bar1 = inspector.getMarkedNode("bar1")
    def bar2 = inspector.getMarkedNode("bar2")

    assertSame(bar, bar1)
    assertSame(inspector.getClass("Foo"), bar2.declaringClass)
  }

  @Test
  void someMoreSemanticAnalysis() {
    def varDef = inspector.getExpression("stat1")
    def varRef = inspector.getExpression("varRef")
    assertSame(varDef.variableExpression, varRef.accessedVariable)

    def method = inspector.getMarkedNode("bar2")
    assertEquals(Inspect.name, method.annotations[0].classNode.name)
  }

  @Test(expected = AstInspectorException.class)
  void throwOnNodeNotFound() {
    inspector.getMethod("notExisting")
  }

  @Test
  void dontThrowOnNodeNotFound() {
    inspector.throwOnNodeNotFound = false
    assertNull(inspector.getMethod("notExisting"))
  }

  @Test(expected=IllegalArgumentException.class)
  void rejectUnsupportedCompilePhase() {
    inspector.compilePhase = CompilePhase.PARSING
  }
}
