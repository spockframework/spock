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

import java.util.List;

import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.*;
import org.codehaus.groovy.control.SourceUnit;

import org.spockframework.runtime.model.*;
import org.spockframework.util.Assert;
import org.spockframework.util.Nullable;
import org.spockframework.util.TextUtil;

/**
 *
 * @author Peter Niederwieser
 */
public class ExpressionInfoBuilder {
  private final String adjustedText;
  private final TextPosition startPos;
  private final List<Object> values;
  private final Integer notRecordedVarNumberBecauseOfException;
  private final Throwable exception;
  private final String[] lines;

  public ExpressionInfoBuilder(String text, TextPosition startPos, List<Object> values, @Nullable Integer notRecordedVarNumberBecauseOfException, @Nullable Throwable exception) {
    this.startPos = startPos;
    this.values = values;
    this.notRecordedVarNumberBecauseOfException = notRecordedVarNumberBecauseOfException;
    this.exception = exception;
    adjustedText = TextUtil.repeatChar(' ', startPos.getColumnIndex()) + text;
    lines = adjustedText.split("\n");
  }

  public ExpressionInfo build() {
    SourceUnit unit = SourceUnit.create("Spec expression", adjustedText);
    unit.parse();
    unit.completePhase();
    unit.convert();

    BlockStatement blockStat = unit.getAST().getStatementBlock();
    Assert.that(blockStat != null && blockStat.getStatements().size() == 1);
    Statement stat = blockStat.getStatements().get(0);
    Assert.that(stat instanceof ExpressionStatement);
    Expression expr = ((ExpressionStatement)stat).getExpression();

    ExpressionInfo exprInfo = new ExpressionInfoConverter(lines).convert(expr);

    // IDEA: rest of this method could be moved to ExpressionInfoConverter (but: might make EIC less testable)
    // IDEA: could make ExpressionInfo immutable
    List<ExpressionInfo> inPostfixOrder = exprInfo.inPostfixOrder(false);
    for (int variableNumber = 0; variableNumber < inPostfixOrder.size(); variableNumber++) {
      ExpressionInfo info = inPostfixOrder.get(variableNumber);
      info.setText(findText(info.getRegion()));
      if (notRecordedVarNumberBecauseOfException!=null && variableNumber == notRecordedVarNumberBecauseOfException) {
        info.setValue(exception);
      } else if (values.size() > variableNumber) { //we have this value
        info.setValue(values.get(variableNumber));
      }else {
        info.setValue(ExpressionInfo.VALUE_NOT_AVAILABLE);
      }
      if (startPos.getLineIndex() > 0)
        info.shiftVertically(startPos.getLineIndex());
    }

    return exprInfo;
  }

  private String findText(TextRegion region) {
    if (region == TextRegion.NOT_AVAILABLE)
      return ExpressionInfo.TEXT_NOT_AVAILABLE;

    try {
      String text = "";
      for (int i = 0; i <= region.getEnd().getLineIndex(); i++) {
        String line = lines[i];
        if (i == region.getEnd().getLineIndex()) line = line.substring(0, region.getEnd().getColumnIndex());
        if (i == region.getStart().getLineIndex()) line = line.substring(region.getStart().getColumnIndex());
        text += line;
        if (i != region.getEnd().getLineIndex()) text += '\n';
      }
      return text;
    } catch (IndexOutOfBoundsException e) {
      return ExpressionInfo.TEXT_NOT_AVAILABLE;
    }
  }
}
