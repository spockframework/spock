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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;

import spock.lang.Speck;
import spock.lang.Predef;

import org.spockframework.mock.*;
import org.spockframework.runtime.SpockRuntime;
import org.spockframework.runtime.ValueRecorder;
import org.spockframework.runtime.model.*;

/**
 * Provides access to frequently used AST nodes.
 *
 * @author Peter Niederwieser
 */

public class AstNodeCache {
  public final ClassNode SpockRuntime = ClassHelper.makeWithoutCaching(SpockRuntime.class);
  public final ClassNode ValueRecorder = ClassHelper.makeWithoutCaching(ValueRecorder.class);
  public final ClassNode Speck = ClassHelper.makeWithoutCaching(Speck.class);
  public final ClassNode Predef = ClassHelper.makeWithoutCaching(Predef.class);

  // annotations and annotation elements
  public final ClassNode SpeckMetadata = ClassHelper.makeWithoutCaching(SpeckMetadata.class);
  public final ClassNode MethodMetadata = ClassHelper.makeWithoutCaching(MethodMetadata.class);
  public final ClassNode BlockMetadata = ClassHelper.makeWithoutCaching(BlockMetadata.class);
  public final ClassNode MethodKind = ClassHelper.makeWithoutCaching(MethodKind.class);
  public final ClassNode BlockKind = ClassHelper.makeWithoutCaching(BlockKind.class);

  // mocking API
  public final ClassNode MockController = ClassHelper.makeWithoutCaching(MockController.class);
  public final ClassNode MockInteraction = ClassHelper.makeWithoutCaching(MockInteraction.class);
  public final ClassNode InteractionBuilder = ClassHelper.makeWithoutCaching(InteractionBuilder.class);
  public final FieldNode DefaultMockFactory_INSTANCE;

  // external types
  public final ClassNode Throwable = ClassHelper.makeWithoutCaching(Throwable.class);

  public AstNodeCache() {
    ClassNode factory = ClassHelper.makeWithoutCaching(DefaultMockFactory.class);
    // since ClassNode.getField(String) does not trigger class node initialization, we call getFields() first
    factory.getFields();
    DefaultMockFactory_INSTANCE = factory.getField("INSTANCE");
  }
}
