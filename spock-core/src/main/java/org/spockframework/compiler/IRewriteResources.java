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

import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.Statement;

import org.spockframework.compiler.model.Block;
import org.spockframework.compiler.model.Method;
import org.spockframework.compiler.model.Spec;

/**
 *
 * @author Peter Niederwieser
 */
public interface IRewriteResources {
  Spec getCurrentSpec();
  Method getCurrentMethod();
  Block getCurrentBlock();

  void defineRecorders(List<Statement> stats, boolean enableErrorCollector);
  VariableExpression captureOldValue(Expression oldValue);
  MethodCallExpression getMockInvocationMatcher();

  AstNodeCache getAstNodeCache();
  String getSourceText(ASTNode node);
  ErrorReporter getErrorReporter();
}
