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

/**
 * Base class for all SpecL AST nodes.
 *
 * @author Peter Niederwieser
 */
public abstract class Node<P extends Node, A> {
  private final P parent;
  private A ast;
  private String name;

  public Node(P parent, A ast) {
    this.parent = parent;
    this.ast = ast;
  }

  /**
   * The parent of this node.
   * @return the parent of this node
   */
  public P getParent() {
    return parent;
  }

  /**
   * The Groovy AST representation of this node.
   * @return the Groovy AST representation of this node
   */
  public A getAst() {
    return ast;
  }

  public void setAst(A ast) {
    this.ast = ast;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ": " + name;
  }

  public abstract void accept(ISpecVisitor visitor) throws Exception;
}
