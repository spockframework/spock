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

package org.spockframework.compiler;

import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

/**
 * An assignment of the form "x = expr", where x is a field and expr is a field
 * initializer expression. The purpose of this class is to make field
 * initializations distinguishable from other assignments while traversing
 * the AST.
 *
 * @author Peter Niederwieser
 */
public class FieldInitializationExpression extends BinaryExpression {
  public FieldInitializationExpression(FieldNode field) {
    super(new FieldExpression(field), Token.newSymbol(Types.ASSIGN, -1, -1), field.getInitialValueExpression());
  }
}
