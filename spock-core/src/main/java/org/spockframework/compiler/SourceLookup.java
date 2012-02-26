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

package org.spockframework.compiler;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.SourceUnit;

public class SourceLookup {
  private final SourceUnit sourceUnit;
  private final Janitor janitor = new Janitor();

  public SourceLookup(SourceUnit sourceUnit) {
    this.sourceUnit = sourceUnit;
  }

  public String lookup(ASTNode node) {
    String text = "";
    for (int i = node.getLineNumber(); i <= node.getLastLineNumber(); i++) {
      String line = sourceUnit.getSample(i, 0, janitor);
      if (line == null) {
        return null; // most probably a Groovy bug, but we'd like to handle this situation gracefully
        // throw new Error(String.format("null line detected; lineNum=%s, expr=%s", i, node.getText()));
      }

      try {
        if (i == node.getLastLineNumber()) line = line.substring(0, node.getLastColumnNumber() - 1);
        if (i == node.getLineNumber()) line = line.substring(node.getColumnNumber() - 1);
        text += line;
        if (i != node.getLastLineNumber()) text += '\n';
      } catch (StringIndexOutOfBoundsException e) {
        return null; // most probably a Groovy bug, but we'd like to handle this situation gracefully
        // throw new RuntimeException(String.format("error getting node text; expr='%s', lineNumber='%s', lastLineNumber='%s', columnNumber='%s', lastColumnNumber='%s', currentLine='%s'", node, node.getLineNumber(), node.getLastLineNumber(), node.getColumnNumber(), node.getLastColumnNumber(), line), e);
      }
    }
    return text.trim();
  }

  public void close() {
    janitor.cleanup();
  }
}
