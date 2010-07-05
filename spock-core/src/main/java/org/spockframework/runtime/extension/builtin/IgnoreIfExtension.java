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

import groovy.lang.Closure;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.extension.ExtensionException;
import org.spockframework.runtime.model.*;
import org.spockframework.util.GroovyRuntimeUtil;

import spock.lang.IgnoreIf;

/**
 * @author Peter Niederwieser
 */
public class IgnoreIfExtension extends AbstractAnnotationDrivenExtension<IgnoreIf> {
	@Override
	public void visitSpecAnnotation(IgnoreIf annotation, SpecInfo spec) {
    doVisit(annotation, spec);
  }

  @Override
  public void visitFeatureAnnotation(IgnoreIf annotation, FeatureInfo feature) {
    doVisit(annotation, feature);
  }

  private void doVisit(IgnoreIf annotation, ISkippable skippable) {
    Closure condition = createCondition(annotation.value());
    Object result = evaluateCondition(condition);
    skippable.setSkipped(GroovyRuntimeUtil.isTruthy(result));
  }

  private Closure createCondition(Class<? extends Closure> clazz) {
    try {
      return clazz.getConstructor(Object.class, Object.class).newInstance(null, null);
    } catch (Exception e) {
      throw new ExtensionException("Failed to instantiate @IgnoreIf condition", e);
    }
  }

  private Object evaluateCondition(Closure condition) {
    try {
      return condition.call();
    } catch (Exception e) {
      throw new ExtensionException("Failed to evaluate @IgnoreIf condition", e);
    }
  }
}