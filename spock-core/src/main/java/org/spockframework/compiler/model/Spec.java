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

package org.spockframework.compiler.model;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;

/**
 * AST node representing a Spock specification. In source
 * code, a Spec corresponds to one class definition extends from class Specification.
 *
 * @author Peter Niederwieser
 */
public class Spec extends Node<Spec, ClassNode> {
  private final List<Field> fields = new ArrayList<Field>();
  private final List<Method> methods = new ArrayList<Method>();
  private final List<FixtureMethod> fixtureMethods = new ArrayList<FixtureMethod>();

  private List<FixtureMethod> setupMethods = new ArrayList<FixtureMethod>();
  private List<FixtureMethod> cleanupMethods = new ArrayList<FixtureMethod>();
  private List<FixtureMethod> setupSpecMethods = new ArrayList<FixtureMethod>();
  private List<FixtureMethod> cleanupSpecMethods = new ArrayList<FixtureMethod>();

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

  public List<FixtureMethod> getFixtureMethods() {
    return fixtureMethods;
  }

  public List<FixtureMethod> getSetupMethods() {
    return setupMethods;
  }

  public void addSetupMethod(FixtureMethod method) {
    setupMethods.add(method);
    fixtureMethods.add(method);
    methods.add(method);
  }

  public List<FixtureMethod> getCleanupMethods() {
    return cleanupMethods;
  }

  public void addCleanupMethod(FixtureMethod method) {
    cleanupMethods.add(method);
    fixtureMethods.add(method);
    methods.add(method);
  }

  public List<FixtureMethod> getSetupSpecMethods() {
    return setupSpecMethods;
  }

  public void addSetupSpecMethod(FixtureMethod method) {
    setupSpecMethods.add(method);
    fixtureMethods.add(method);
    methods.add(method);
  }

  public List<FixtureMethod> getCleanupSpecMethods() {
    return cleanupSpecMethods;
  }

  public void addCleanupSpecMethod(FixtureMethod method) {
    cleanupSpecMethods.add(method);
    fixtureMethods.add(method);
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
