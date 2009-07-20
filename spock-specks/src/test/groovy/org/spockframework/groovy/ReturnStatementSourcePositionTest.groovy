/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.groovy

import org.spockframework.util.inspector.AstInspector
import org.codehaus.groovy.ast.stmt.ReturnStatement

class ReturnStatementSourcePositionTest extends GroovyTestCase {
  void test() {
    def inspector = new AstInspector()

    inspector.load("""
class Foo {
  def bar() {
    return
  }
}
    """)

    def method = inspector.getMethod("bar");
    def stat = method.code.statements[0]
    assert stat instanceof ReturnStatement
    assert stat.lineNumber == 4
    assert stat.columnNumber == 5
    assert stat.lastLineNumber == 4
    assert stat.lastColumnNumber == 11
  }
}