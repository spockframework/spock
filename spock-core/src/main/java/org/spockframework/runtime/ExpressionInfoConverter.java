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

package org.spockframework.runtime;

import java.util.*;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.classgen.BytecodeExpression;
import org.codehaus.groovy.syntax.Types;

import org.spockframework.runtime.model.*;
import org.spockframework.util.AbstractExpressionConverter;

// NOTE: expressions which don't produce a value are handled as follows:
// - exactly one child: don't create ExpressionInfo (replace with child's ExpressionInfo)
// - no child or more than one child: create ExpressionInfo and set hasValue to false
// NOTE: because Expression's followed by a token often have incorrect column number, we always
// search for the token backwards from the subsequent expression
// IDEA: combine with value population because values might provide some additional
// hints about the real nature of a node (as we are only in phase CONVERSION)
public class ExpressionInfoConverter extends AbstractExpressionConverter<ExpressionInfo> {
  private final String[] lines;

  public ExpressionInfoConverter(String[] lines) {
    this.lines = lines;
  }

  public void visitMethodCallExpression(MethodCallExpression expr) {
    TextPosition anchor = TextPosition.startOf(expr.getMethod());

    List<ExpressionInfo> children = new ArrayList<ExpressionInfo>();
    if (!expr.isImplicitThis())
      children.add(convert(expr.getObjectExpression()));
    children.add(convert(expr.getMethod()));
    children.add(convert(expr.getArguments()));

    result = new ExpressionInfo(
        TextRegion.of(expr),
        anchor,
        expr.getMethodAsString(),
        children);
  }

  public void visitBytecodeExpression(BytecodeExpression expr) {
    unsupported();
  }

  public void visitStaticMethodCallExpression(StaticMethodCallExpression expr) {
    unsupported(); // still a MethodCallExpression in phase conversion
  }

  public void visitConstructorCallExpression(ConstructorCallExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        "<init>",
        convert(expr.getArguments()));
  }

  @SuppressWarnings("unchecked")
  public void visitArgumentlistExpression(ArgumentListExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        null,
        convertAll(expr.getExpressions())
    ).setRelevant(false);
  }

  public void visitPropertyExpression(PropertyExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr.getProperty()),
        expr.getPropertyAsString(),
        expr.isImplicitThis() ?
            Collections.<ExpressionInfo>emptyList() :
            Collections.singletonList(convert(expr.getObjectExpression())));
  }

  public void visitAttributeExpression(AttributeExpression expr) {
    visitPropertyExpression(expr);
  }

  public void visitFieldExpression(FieldExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        expr.getFieldName()
    );
  }

  public void visitMethodPointerExpression(MethodPointerExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr.getMethodName()),
        expr.getMethodName().getText(),
        convert(expr.getExpression()),
        convert(expr.getMethodName())
    ).setRelevant(false);
  }

  public void visitVariableExpression(VariableExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        expr.getName()
    ).setRelevant(expr != VariableExpression.THIS_EXPRESSION
        && expr != VariableExpression.SUPER_EXPRESSION);
  }

  public void visitDeclarationExpression(DeclarationExpression expression) {
    unsupported();
  }

  public void visitRegexExpression(RegexExpression expr) {
    unsupported(); // unused AST node
  }

  public void visitConstantExpression(ConstantExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        expr.getConstantName()
    ).setRelevant(false);
  }

  public void visitClassExpression(ClassExpression expr) {
    // also used in phase conversion, e.g. in instanceof expression
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        null
    ).setRelevant(false);
  }

  public void visitBinaryExpression(BinaryExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        expr.getOperation().getType() == Types.LEFT_SQUARE_BRACKET ?
            startOf("[", expr.getRightExpression()) : // workaround for the fact that Token.startLine == 0 for token [
            TextPosition.startOf(expr.getOperation()),
        expr.getOperation().getText(),
        convert(expr.getLeftExpression()),
        convert(expr.getRightExpression())
    );
  }

  public void visitUnaryMinusExpression(UnaryMinusExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        "-",
        convert(expr.getExpression()));
  }

  public void visitUnaryPlusExpression(UnaryPlusExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        "+",
        convert(expr.getExpression()));
  }

  public void visitNotExpression(NotExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        "!",
        convert(expr.getExpression()));
  }

  public void visitBitwiseNegationExpression(BitwiseNegationExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        "^",
        convert(expr.getExpression()));
  }

  @SuppressWarnings("unchecked")
  public void visitListExpression(ListExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        "[]",
        convertAll(expr.getExpressions())
    ).setRelevant(false);
  }

  public void visitRangeExpression(RangeExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        startOf("..", expr.getTo()),
        "..",
        convert(expr.getFrom()),
        convert(expr.getTo())
    ).setRelevant(false);
  }

  @SuppressWarnings("unchecked")
  public void visitMapExpression(MapExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        "[:]",
        convertAll(expr.getMapEntryExpressions())
    ).setRelevant(false);
  }

  public void visitMapEntryExpression(MapEntryExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        startOf(":", expr.getValueExpression()),
        null,
        convert(expr.getKeyExpression()),
        convert(expr.getValueExpression())
    );
  }

  @SuppressWarnings("unchecked")
  public void visitGStringExpression(GStringExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        "\"\"",
        convertAll(expr.getValues())
    ).setRelevant(false);
  }

  public void visitTernaryExpression(TernaryExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        startOf("?", expr.getTrueExpression()),
        "?:",
        convertAll(
            Arrays.asList(
                expr.getBooleanExpression(),
                expr.getTrueExpression(),
                expr.getFalseExpression()))
    ).setRelevant(false);
  }

  public void visitShortTernaryExpression(ElvisOperatorExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        startOf("?", expr.getFalseExpression()),
        "?:",
        convert(expr.getTrueExpression()),
        convert(expr.getFalseExpression())
    ).setRelevant(false);
  }

  public void visitPrefixExpression(PrefixExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        expr.getOperation().getText(),
        convert(expr.getExpression()));
  }

  public void visitPostfixExpression(PostfixExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        expr.getOperation().getText(),
        convert(expr.getExpression()));
  }

  public void visitBooleanExpression(BooleanExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        null,
        convert(expr.getExpression())
    ).setRelevant(false);
  }

  public void visitClosureExpression(ClosureExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        "{->}"
    ).setRelevant(false);
  }

  @SuppressWarnings("unchecked")
  public void visitTupleExpression(TupleExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        null,
        convertAll(expr.getExpressions())
    ).setRelevant(false);
  }

  public void visitCastExpression(CastExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        "as",
        convert(expr.getExpression())
    ).setRelevant(false);
  }

  public void visitClosureListExpression(ClosureListExpression expr) {
    unsupported(); // cannot occur in assertion
  }

  @SuppressWarnings("unchecked")
  public void visitArrayExpression(ArrayExpression expr) {
    List<ExpressionInfo> children = convertAll(expr.getExpressions());
    children.addAll(convertAll(expr.getSizeExpression()));

    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        null,
        children
    ).setRelevant(false);
  }

  public void visitSpreadExpression(SpreadExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        "*",
        convert(expr.getExpression())
    ).setRelevant(false);

  }

  public void visitSpreadMapExpression(SpreadMapExpression expr) {
    result = new ExpressionInfo(
        TextRegion.of(expr),
        TextPosition.startOf(expr),
        "*:"
    );
  }

  // searches for token backwards from beginning of node (exclusive)
  // IDEA: move to class TextPosition?
  private TextPosition startOf(String token, ASTNode node) {
    // try to compensate for the fact that getLastLineNumber() is sometimes wrong
    int lastLineIndex = Math.max(node.getLineNumber(), node.getLastLineNumber()) - 1;
    
    for (int lineIndex = lastLineIndex; lineIndex >= 0; lineIndex--) {
      int columnIndex = lineIndex == lastLineIndex ?
          lines[lineIndex].lastIndexOf(token, node.getColumnNumber() - 1) :
          lines[lineIndex].lastIndexOf(token);
      if (columnIndex != -1)
        return TextPosition.create(lineIndex + 1, columnIndex + 1);
    }

    throw new IllegalArgumentException(String.format("token %s not found in expression", token));
  }
}