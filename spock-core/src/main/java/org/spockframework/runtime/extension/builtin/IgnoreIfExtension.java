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

package org.spockframework.runtime.extension.builtin;

import org.opentest4j.TestAbortedException;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.*;
import spock.lang.IgnoreIf;

import groovy.lang.Closure;

/**
 * @author Peter Niederwieser
 */
public class IgnoreIfExtension extends ConditionalExtension<IgnoreIf> {
  @Override
  protected Class<? extends Closure> getConditionClass(IgnoreIf annotation) {
    return annotation.value();
  }

  @Override
  protected void specConditionResult(boolean result, IgnoreIf annotation, SpecInfo spec) {
    if (result) {
      spec.skip("Ignored via @IgnoreIf");
    }
  }

  @Override
  protected void featureConditionResult(boolean result, IgnoreIf annotation, FeatureInfo feature) {
    if (result) {
      feature.skip("Ignored via @IgnoreIf");
    }
  }

  @Override
  protected void iterationConditionResult(boolean result, IgnoreIf annotation, IMethodInvocation invocation) {
    if (result) {
      throw new TestAbortedException("Ignored via @IgnoreIf");
    }
  }
}
