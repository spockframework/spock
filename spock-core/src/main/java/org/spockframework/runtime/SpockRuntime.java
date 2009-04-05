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
