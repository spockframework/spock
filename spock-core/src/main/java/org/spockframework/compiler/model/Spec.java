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

import java.util.*;

import org.codehaus.groovy.ast.ClassNode;

/**
 * AST node representing a Spock specification. In source
 * code, a Spec corresponds to one class definition extends from class Specification.
 *
 * @author Peter Niederwieser
 */
public class Spec extends Node<Spec, ClassNode> {
  private final List<Field> fields = new ArrayList<>();
  private final List<Method> methods = new ArrayList<>();

  private FixtureMethod initializerMethod;
  private FixtureMethod sharedInitializerMethod;

  private FixtureMethod setupMethod;
  private FixtureMethod cleanupMethod;
  private FixtureMethod setupSpecMethod;
  private FixtureMethod cleanupSpecMethod;

  public Spec(ClassNode code) {
    super(null, code);
    setName(code.getName());
  }

  public List<Field> getFields() {
    return fields;
  }

  public List<Method> getMethods() {
    return methods;
  }

  public FixtureMethod getInitializerMethod() {
    return initializerMethod;
  }

  public void setInitializerMethod(FixtureMethod method) {
    initializerMethod = method;
    methods.add(method);
  }

  public FixtureMethod getSharedInitializerMethod() {
    return sharedInitializerMethod;
  }

  public void setSharedInitializerMethod(FixtureMethod method) {
    sharedInitializerMethod = method;
    methods.add(method);
  }

  public FixtureMethod getSetupMethod() {
    return setupMethod;
  }

  public void setSetupMethod(FixtureMethod method) {
    setupMethod = method;
    methods.add(method);
  }

  public FixtureMethod getCleanupMethod() {
    return cleanupMethod;
  }

  public void setCleanupMethod(FixtureMethod method) {
    cleanupMethod = method;
    methods.add(method);
  }

  public FixtureMethod getSetupSpecMethod() {
    return setupSpecMethod;
  }

  public void setSetupSpecMethod(FixtureMethod method) {
    setupSpecMethod = method;
    methods.add(method);
  }

  public FixtureMethod getCleanupSpecMethod() {
    return cleanupSpecMethod;
  }

  public void setCleanupSpecMethod(FixtureMethod method) {
    cleanupSpecMethod = method;
    methods.add(method);
  }

  @Override
  public void accept(ISpecVisitor visitor) throws Exception {
    visitor.visitSpec(this);
    for (Field f : fields) f.accept(visitor);
    for (Method m : methods) m.accept(visitor);
    visitor.visitSpecAgain(this);
  }
}
