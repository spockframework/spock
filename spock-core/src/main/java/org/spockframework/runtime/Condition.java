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

import org.spockframework.runtime.model.ExpressionInfo;
import org.spockframework.runtime.model.TextPosition;

import java.util.regex.Pattern;

/**
 * Runtime representation of an evaluated condition.
 *
 * @author Peter Niederwieser
 */
public class Condition {
  private static final Pattern pattern = Pattern.compile("\\s*\n\\s*");

  private final String text;
  private final TextPosition position;
  private final Iterable<Object> values;
  private final String message;

  public Condition(String text, TextPosition position, Iterable<Object> values, String message) {
    this.text = text;
    this.position = position;
    this.values = values;
    this.message = message;
  }

  public String getText() {
    return text;
  }

  public TextPosition getPosition() {
    return position;
  }

  public Iterable<Object> getValues() {
    return values;
  }

  public String getMessage() {
    return message;
  }

  public String render() {
    if (text == null)
      return "(No detail information available)\n";
    
    if (message != null)
      return String.format("%s\n\nYour message: %s\n", flatten(text), message);
    
    ExpressionInfo exprInfo = new ExpressionInfoBuilder(flatten(text), TextPosition.create(1, 1), values).build();
    return ExpressionInfoRenderer.render(exprInfo);
  }

  private static String flatten(String text) {
    return pattern.matcher(text).replaceAll(" ");
  }
}
