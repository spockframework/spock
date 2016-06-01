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
import java.util.regex.Pattern;

import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.runtime.model.TextPosition;
import org.spockframework.util.Nullable;

/**
 * Runtime representation of an evaluated condition.
 *
 * @author Peter Niederwieser
 */
public class Condition {
  private static final Pattern pattern = Pattern.compile("\\s*\n\\s*");

  private final List<Object> values;
  private final String text;
  private final TextPosition position;
  private final String message;
  private final Integer notRecordedVarNumberBecauseOfException;
  private final Throwable exception;

  private volatile ExpressionInfo expression;
  private volatile String rendering;

  public Condition(@Nullable List<Object> values, @Nullable String text, TextPosition position,
      @Nullable String message, @Nullable Integer notRecordedVarNumberBecauseOfException, @Nullable Throwable exception) {
    this.text = text;
    this.position = position;
    this.values = values;
    this.message = message;
    this.notRecordedVarNumberBecauseOfException = notRecordedVarNumberBecauseOfException;
    this.exception = exception;
  }

  @Nullable
  public List<Object> getValues() {
    return values;
  }

  @Nullable
  public String getText() {
    return text;
  }

  public TextPosition getPosition() {
    return position;
  }

  @Nullable
  public String getMessage() {
    return message;
  }

  @Nullable
  public ExpressionInfo getExpression() {
    if (expression == null) {
      createExpression();
    }

    return expression;
  }

  public String getRendering() {
    if (rendering == null) {
      createRendering();
    }

    return rendering;
  }

  private void createExpression() {
    if (text == null || values == null) return;
    expression = new ExpressionInfoBuilder(flatten(text), TextPosition.create(1, 1), values, notRecordedVarNumberBecauseOfException, exception).build();
  }

  private void createRendering() {
    StringBuilder builder = new StringBuilder();

    if (getExpression() != null) {
      ExpressionInfoValueRenderer.render(expression);
      builder.append(ExpressionInfoRenderer.render(expression));
    } else if (text != null) {
      builder.append(flatten(text));
      builder.append("\n");
    } else {
      builder.append("(Source code not available)\n");
    }

    if (message != null) {
      builder.append("\n");
      builder.append(message);
      builder.append("\n");
    }

    rendering = builder.toString();
  }

  private String flatten(String text) {
    return pattern.matcher(text).replaceAll(" ");
  }
}
