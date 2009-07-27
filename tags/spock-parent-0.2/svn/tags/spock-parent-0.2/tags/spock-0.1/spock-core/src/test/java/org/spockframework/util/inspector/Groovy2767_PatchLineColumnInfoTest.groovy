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

package org.spockframework.util.inspector

class Groovy2767_PatchLineColumnInfoTest extends GroovyTestCase {
  def inspector = new AstInspector()
  def exprs
  def idx = 0

  void setUp() {
    inspector.load("""
true
false
null
""
this
super
         true
         false
         null
         ""
         this
         super
    """)

    exprs = inspector.scriptExpressions
  }

  void testLineColumnInfo() {
    col 1,5
    col 1,6
    col 1,5
    col 1,3
    col 1,5
    col 1,6
    col 10,14
    col 10,15
    col 10,14
    col 10,12
    col 10,14
    col 10,15
  }

  void testCorrectComparisons() {
    exprs[0].isTrueExpression()
    exprs[1].isFalseExpression()
    exprs[2].isNullExpression()
    exprs[3].isEmptyStringExpression()
    exprs[4].isThisExpression()
    exprs[5].isSuperExpression()

    exprs[6].isTrueExpression()
    exprs[7].isFalseExpression()
    exprs[8].isNullExpression()
    exprs[9].isEmptyStringExpression()
    exprs[10].isThisExpression()
    exprs[11].isSuperExpression()
  }

  void testIncorrectComparisons() {
    !exprs[0].isFalseExpression()
    !exprs[1].isTrueExpression()
    !exprs[2].isEmptyStringExpression()
    !exprs[3].isNullExpression()
    !exprs[4].isSuperExpression()
    !exprs[5].isThisExpression()

    !exprs[6].isFalseExpression()
    !exprs[7].isTrueExpression()
    !exprs[8].isEmptyStringExpression()
    !exprs[9].isNullExpression()
    !exprs[10].isSuperExpression()
    !exprs[11].isThisExpression()
  }

  private col(col, lastCol) {
    pos(idx + 2, col, idx + 2, lastCol)
  }

  private pos(line, col, lastLine, lastCol) {
    def expr = exprs[idx++]
    assertEquals([line, col, lastLine, lastCol],
      [expr.lineNumber, expr.columnNumber, expr.lastLineNumber, expr.lastColumnNumber])
  }
}
