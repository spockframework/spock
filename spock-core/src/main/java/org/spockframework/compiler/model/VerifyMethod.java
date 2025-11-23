/*
 * Copyright 2025 the original author or authors.
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

package org.spockframework.compiler.model;

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.stmt.Statement;
import org.spockframework.compiler.AstUtil;
import org.spockframework.compiler.ErrorReporter;

import java.util.List;

/**
 * AST node representing a @Verify or @VerifyAll method.
 */
public class VerifyMethod extends Method {
  public VerifyMethod(MethodNode code) {
    this(null, code);
  }

  public VerifyMethod(Spec parent, MethodNode code) {
    super(parent, code);
    VerifyBlock block = new VerifyBlock(this);
    List<Statement> stats = AstUtil.getStatements(getAst());
    block.getAst().addAll(stats);
    stats.clear();
    addBlock(block);
  }

  public static boolean verifyReturnType(MethodNode method, ErrorReporter errorReporter) {
    if (!method.isVoidMethod() && !method.isDynamicReturnType()) {
      errorReporter.error("Verification helper method '%s' must have a void return type or use a dynamic return type ('def' or no declared type).", method.getName());
      return false;
    }
    return true;
  }
}
