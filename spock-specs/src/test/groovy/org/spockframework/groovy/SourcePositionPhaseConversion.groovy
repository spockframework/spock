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

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.spockframework.util.inspector.AstInspector
import spock.lang.Specification

/**
 * @author Peter Niederwieser
 */
class SourcePositionPhaseConversion extends Specification {
  AstInspector inspector = new AstInspector()

  def "subscript operator"() {
    inspector.load("""
foo[0]
    """)

    expect:
    inspector.scriptExpressions[0].operation.startColumn == 4
  }

  def "PropertyExpression that will become ClassExpression"() {
    inspector.load("""
List.class
    """)

    ASTNode expr = inspector.scriptExpressions[0]

    expect:
    expr instanceof PropertyExpression
    expr.lineNumber == 2
    expr.columnNumber == 1
    expr.lastLineNumber == 2
    expr.lastColumnNumber == 11
  }
}
