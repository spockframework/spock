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

  private FixtureMethod setup;
  private FixtureMethod cleanup;
  private FixtureMethod setupSpec;
  private FixtureMethod cleanupSpec;

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

  public FixtureMethod getSetup() {
    return setup;
  }

  public void setSetup(FixtureMethod setup) {
    this.setup = setup;
    methods.add(setup);
    fixtureMethods.add(setup);
  }

  public FixtureMethod getCleanup() {
    return cleanup;
  }

  public void setCleanup(FixtureMethod cleanup) {
    this.cleanup = cleanup;
    methods.add(cleanup);
    fixtureMethods.add(cleanup);

  }

  public FixtureMethod getSetupSpec() {
    return setupSpec;
  }

  public void setSetupSpec(FixtureMethod setupSpec) {
    this.setupSpec = setupSpec;
    methods.add(setupSpec);
    fixtureMethods.add(setupSpec);
  }

  public FixtureMethod getCleanupSpec() {
    return cleanupSpec;
  }

  public void setCleanupSpec(FixtureMethod cleanupSpec) {
    this.cleanupSpec = cleanupSpec;
    methods.add(cleanupSpec);
    fixtureMethods.add(cleanupSpec);
  }

  @Override
  public void accept(ISpecVisitor visitor) throws Exception {
    visitor.visitSpec(this);
    for (Field f : fields) f.accept(visitor);
    for (Method m : methods) m.accept(visitor);
    visitor.visitSpecAgain(this);
  }
}
