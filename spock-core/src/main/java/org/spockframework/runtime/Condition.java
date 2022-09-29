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

package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import org.spockframework.util.Nullable;

import java.io.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runtime representation of an evaluated condition.
 *
 * @author Peter Niederwieser
 */
public class Condition implements Serializable {
  private static final long serialVersionUID = 1L;

  private static final Pattern pattern =
    Pattern.compile("(?<backslashesToEscape>(?:\\\\\\\\)+)|(?<stripBackslash>\\\\\n)|(?<whitespacesToCollapse>\\s*\n\\s*)");

  private final transient List<Object> values;
  private final String text;
  private final TextPosition position;
  private final String message;
  private final Integer notRecordedVarNumberBecauseOfException;
  private final Throwable exception;

  private transient volatile ExpressionInfo expression;
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
    String stripAndFlattenText = stripAndFlatten(text).toString();
    expression = new ExpressionInfoBuilder(stripAndFlattenText, TextPosition.create(1, 1), values, notRecordedVarNumberBecauseOfException, exception).build();
  }

  private void createRendering() {
    StringBuilder builder = new StringBuilder();

    if (getExpression() != null) {
      ExpressionInfoValueRenderer.render(expression);
      builder.append(ExpressionInfoRenderer.render(expression));
    } else if (text != null) {
      builder.append(stripAndFlatten(text));
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

  private static CharSequence stripAndFlatten(String text) {
    Matcher m = pattern.matcher(text);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String replacement;
      String backslashesToEscape = m.group("backslashesToEscape");
      if (backslashesToEscape != null) {
        replacement = backslashesToEscape + backslashesToEscape;
      } else if (m.group("whitespacesToCollapse") != null){
        replacement = " ";
      } else {
        replacement = "";
      }
      m.appendReplacement(sb, replacement);
    }
    m.appendTail(sb);
    return sb;
  }

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    // create the rendering so that it is available for serialization
    getRendering();
    out.defaultWriteObject();
  }
}
