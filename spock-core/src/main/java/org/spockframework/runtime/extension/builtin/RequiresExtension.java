/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.runtime.extension.builtin;

import org.opentest4j.TestAbortedException;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.*;
import spock.lang.Requires;

import groovy.lang.Closure;

/**
 * @author Peter Niederwieser
 */
public class RequiresExtension extends ConditionalExtension<Requires> {
  @Override
  protected Class<? extends Closure> getConditionClass(Requires annotation) {
    return annotation.value();
  }

  @Override
  protected void specConditionResult(boolean result, Requires annotation, SpecInfo spec) {
    if (!result) {
      spec.skip("Ignored via @Requires");
    }
  }

  @Override
  protected void featureConditionResult(boolean result, Requires annotation, FeatureInfo feature) {
    if (!result) {
      feature.skip("Ignored via @Requires");
    }
  }

  @Override
  protected void iterationConditionResult(boolean result, Requires annotation, IMethodInvocation invocation) {
    if (!result) {
      throw new TestAbortedException("Ignored via @Requires");
    }
  }
}
