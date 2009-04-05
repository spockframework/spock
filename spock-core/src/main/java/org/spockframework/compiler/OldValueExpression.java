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

import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.expr.Expression;

/**
 * Used to represent the argument to Predef.old() once it has been processed
 * by IRewriteResourceProvider.captureOldValue(). The original expression is
 * kept in case ConditionRewriter still needs it.
 * 
 * @author Peter Niederwieser
 */
public class OldValueExpression extends VariableExpression {
  private Expression originalExpression;

  public OldValueExpression(Expression originalExpression, String substitutedVariable) {
    super(substitutedVariable);
    this.originalExpression = originalExpression;
  }

  public Expression getOrginalExpression() {
    return originalExpression;
  }
}
