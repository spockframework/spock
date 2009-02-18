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

package org.spockframework.runtime;

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import org.spockframework.runtime.model.TextPosition;

/**
 * IDEA:
 * Make SpockRuntime accessible via ThreadLocal
 * Potential benefits:
 * - Some functionality can be moved from compiler to runtime (mock creation,
 *   calls to MockController.verify(), etc.)
 * - Facilitates testing of Spock
 * - Extensions and Speck runner get access to context information like current
 *   feature method name, current block, etc.
 * - Context information is accessible from everywhere, w/o possessing a
 *   reference to the Speck instance
 * - Context information no longer coupled to Speck instantiation strategy
 * - Single entry-point for stateless (e.g. SpockRuntime.verifyCondition()) and
 *   stateful (e.g. MockController.enterScope()) runtime calls
 * 
 * @author Peter Niederwieser
 */
public abstract class SpockRuntime {
  public static final String VERIFY_CONDITION = "verifyCondition";

  public static void verifyCondition(ValueRecorder recorder, Object condition, String text, int line, int column) {
    if (!DefaultTypeTransformation.castToBoolean(condition))
      throw new ConditionNotSatisfiedError(
          new Condition(text, TextPosition.create(line, column), recorder));
  }

  public static final String FEATURE_METHOD_CALLED = "featureMethodCalled";
  
  public static void featureMethodCalled() {
    throw new InvalidSpeckError("Feature methods cannot be called from user code");
  }
}
