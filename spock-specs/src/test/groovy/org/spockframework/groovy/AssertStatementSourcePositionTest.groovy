/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.groovy

import org.spockframework.util.inspector.AstInspector
import org.codehaus.groovy.ast.stmt.AssertStatement
import spock.lang.Specification

class AssertStatementSourcePositionTest extends Specification {
  void test() {
    def inspector = new AstInspector()

    inspector.load("""
class Foo {
  def bar() {
    assert 1 == 2
  }
}
    """)

    def method = inspector.getMethod("bar");
    def stat = method.code.statements[0]

    expect:
    stat instanceof AssertStatement
    stat.lineNumber == 4
    stat.columnNumber == 5
    stat.lastLineNumber == 4
    stat.lastColumnNumber == 18

    def expr = stat.booleanExpression
    expr.lineNumber == 4
    expr.columnNumber == 12
    expr.lastLineNumber == 4
    expr.lastColumnNumber == 18
  }
}
