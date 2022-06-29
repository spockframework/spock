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

package org.spockframework.compiler.model;

import org.codehaus.groovy.ast.*;

/**
 * AST node representing a user-defined instance field.
 * (A generated field underlying a user-defined property also counts as such.)
 *
 * @author Peter Niederwieser
 */
public class Field extends Node<Spec, FieldNode> {
  private final int ordinal;
  private final boolean hasInitialExpression;
  private boolean isShared;
  private PropertyNode owner;

  public Field(Spec parent, FieldNode ast, int ordinal) {
    super(parent, ast);
    setName(ast.getName());
    this.ordinal = ordinal;
    this.hasInitialExpression = ast.hasInitialExpression();
  }

  public int getOrdinal() {
    return ordinal;
  }

  public boolean isShared() {
    return isShared;
  }

  public void setShared(boolean shared) {
    isShared = shared;
  }

  // null for a true field that has no associated property
  public PropertyNode getOwner() {
    return owner;
  }

  public void setOwner(PropertyNode owner) {
    this.owner = owner;
  }

  public boolean hasInitialExpression() {
    return hasInitialExpression;
  }

  @Override
  public void accept(ISpecVisitor visitor) throws Exception {
    visitor.visitField(this);
  }
}
