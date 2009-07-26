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
 * AST node representing a Spock specification (abbreviated to "Speck"). In source
 * code, a Speck corresponds to one class definition marked with @Speck.
 *
 * @author Peter Niederwieser
 */
public class Speck extends Node<Speck, ClassNode> {
  private final List<Method> methods = new ArrayList<Method>();
  private final List<FixtureMethod> fixtureMethods = new ArrayList<FixtureMethod>();
  private FixtureMethod setup;
  private FixtureMethod cleanup;
  private FixtureMethod setupSpeck;
  private FixtureMethod cleanupSpeck;

  public Speck(ClassNode code) {
    super(null, code);
    setName(code.getName());
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

  public FixtureMethod getSetupSpeck() {
    return setupSpeck;
  }

  public void setSetupSpeck(FixtureMethod setupSpeck) {
    this.setupSpeck = setupSpeck;
    methods.add(setupSpeck);
    fixtureMethods.add(setupSpeck);
  }

  public FixtureMethod getCleanupSpeck() {
    return cleanupSpeck;
  }

  public void setCleanupSpeck(FixtureMethod cleanupSpeck) {
    this.cleanupSpeck = cleanupSpeck;
    methods.add(cleanupSpeck);
    fixtureMethods.add(cleanupSpeck);
  }

  @Override
  public void accept(ISpeckVisitor visitor) throws Exception {
    visitor.visitSpeck(this);
    for (Method m : methods) m.accept(visitor);
    visitor.visitSpeckAgain(this);
  }
}
