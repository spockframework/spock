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
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.control.CompilePhase
import org.spockframework.runtime.GroovyRuntimeUtil
import org.spockframework.util.inspector.AstInspector
import spock.lang.Requires
import spock.lang.Specification

/**
 * A ...

 * @author Peter Niederwieser
 */
class SourcePositionPhaseSemanticAnalysis extends Specification {
  AstInspector inspector = new AstInspector(CompilePhase.SEMANTIC_ANALYSIS)

  // ConditionRewriter.visitClassExpression() relies on this
  def "implicit class expressions have no line/column info"() {
    inspector.load("""
import static java.lang.Thread.State.*
NEW
    """)

    ASTNode node = inspector.scriptExpressions[0].objectExpression

    expect:
    node.lineNumber == -1
    node.columnNumber == -1
    node.lastLineNumber == -1
    node.lastColumnNumber == -1
  }

  // ConditionRewriter.visitClassExpression() relies on this
  def "explicit class expressions have line/column info"() {
    inspector.load("""
import java.lang.Thread.State
State.NEW
    """)

    ASTNode node = inspector.scriptExpressions[0].objectExpression

    expect:
    node.lineNumber == 3
    node.columnNumber == 1
    node.lastLineNumber == 3
    node.lastColumnNumber == 6
  }

  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION == 2 })  //lastColumnNumber value fixed in new parser in Groovy 3
  def "short-form class literals have accurate line/column info (Groovy 2)"() {
    inspector.load("""
List
List .methods
println(  List  )
    """)

    expect:
    ASTNode node1 = inspector.scriptExpressions[0]
    node1 instanceof ClassExpression
    node1.lineNumber == 2
    node1.columnNumber == 1
    node1.lastLineNumber == 2
    node1.lastColumnNumber == 5

    and:
    ASTNode node2 = inspector.scriptExpressions[1].objectExpression
    node2 instanceof ClassExpression
    node2.lineNumber == 3
    node2.columnNumber == 1
    node2.lastLineNumber == 3
    node2.lastColumnNumber == 5

    and:
    ASTNode node3 = inspector.scriptExpressions[2].arguments.expressions[0]
    node3 instanceof ClassExpression
    node3.lineNumber == 4
    node3.columnNumber == 11
    node3.lastLineNumber == 4
    node3.lastColumnNumber == 17 // should be: 15
  }

  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION >= 3 })
  def "short-form class literals have accurate line/column info"() {
    inspector.load("""
List
List .methods
println(  List  )
    """)

    expect:
    ASTNode node1 = inspector.scriptExpressions[0]
    node1 instanceof ClassExpression
    node1.lineNumber == 2
    node1.columnNumber == 1
    node1.lastLineNumber == 2
    node1.lastColumnNumber == 5

    and:
    ASTNode node2 = inspector.scriptExpressions[1].objectExpression
    node2 instanceof ClassExpression
    node2.lineNumber == 3
    node2.columnNumber == 1
    node2.lastLineNumber == 3
    node2.lastColumnNumber == 5

    and:
    ASTNode node3 = inspector.scriptExpressions[2].arguments.expressions[0]
    node3 instanceof ClassExpression
    node3.lineNumber == 4
    node3.columnNumber == 11
    node3.lastLineNumber == 4
    node3.lastColumnNumber == 15
  }

  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION == 2 })  //column number changed in Groovy 3
  def "long-form class literals have accurate line/column info (Groovy 2)"() {
    inspector.load("""
List.class
List.class.methods
    """)

    expect:
    ASTNode node1 = inspector.scriptExpressions[0]
    node1 instanceof ClassExpression
    node1.lineNumber == 2
    node1.columnNumber == 1
    node1.lastLineNumber == 2
    node1.lastColumnNumber == 11

    and:
    ASTNode node2 = inspector.scriptExpressions[1].objectExpression
    node2 instanceof ClassExpression
    node2.lineNumber == 3
    node2.columnNumber == 1
    node2.lastLineNumber == 3
    node2.lastColumnNumber == 11
  }

  @Requires({ GroovyRuntimeUtil.MAJOR_VERSION >= 3 })
  def "long-form class literals have accurate line/column info"() {
    inspector.load("""
List.class
List.class.methods
    """)

    expect:
    ASTNode node1 = inspector.scriptExpressions[0]
    node1 instanceof ClassExpression
    node1.lineNumber == 2
    node1.columnNumber == 1
    node1.lastLineNumber == 2
    node1.lastColumnNumber == 11

    and:
    ASTNode node2 = inspector.scriptExpressions[1].objectExpression
    node2 instanceof ClassExpression
    node2.lineNumber == 3
    node2.columnNumber == 5
    node2.lastLineNumber == 3
    node2.lastColumnNumber == 11
  }
}
