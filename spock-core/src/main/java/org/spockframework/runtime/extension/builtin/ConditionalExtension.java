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
import groovy.lang.MissingPropertyException;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.extension.ExtensionException;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.lang.annotation.Annotation;
import java.util.List;

import static java.util.Collections.emptyList;

public abstract class ConditionalExtension<T extends Annotation> extends AbstractAnnotationDrivenExtension<T> {
  protected abstract Class<? extends Closure> getConditionClass(T annotation);

  protected void specConditionResult(boolean result, T annotation, SpecInfo spec) {
    throw new UnsupportedOperationException();
  }

  protected void featureConditionResult(boolean result, T annotation, FeatureInfo feature) {
    throw new UnsupportedOperationException();
  }

  protected void iterationConditionResult(boolean result, T annotation, IMethodInvocation invocation) throws Throwable {
    throw new UnsupportedOperationException();
  }

  @Override
  public void visitSpecAnnotation(T annotation, SpecInfo spec) {
    Closure condition = createCondition(annotation);
    Object result = evaluateCondition(condition);
    specConditionResult(GroovyRuntimeUtil.isTruthy(result), annotation, spec);
  }

  @Override
  public void visitFeatureAnnotation(T annotation, FeatureInfo feature) {
    Closure condition = createCondition(annotation);

    try {
      Object result = evaluateCondition(condition);
      featureConditionResult(GroovyRuntimeUtil.isTruthy(result), annotation, feature);
    } catch (ExtensionException ee) {
      if (!(ee.getCause() instanceof MissingPropertyException)) {
        throw ee;
      }
      MissingPropertyException mpe = (MissingPropertyException) ee.getCause();
      if (!feature.getDataVariables().contains(mpe.getProperty())) {
        throw ee;
      }
      feature.getFeatureMethod().addInterceptor(invocation -> {
        List<String> dataVariables = invocation.getFeature().getDataVariables();
        Object[] dataValues = invocation.getIteration().getDataValues();
        Object result = evaluateCondition(condition, dataVariables, dataValues);
        iterationConditionResult(GroovyRuntimeUtil.isTruthy(result), annotation, invocation);
        invocation.proceed();
      });
    }
  }

  private Closure createCondition(T annotation) {
    Class<? extends Closure> clazz = getConditionClass(annotation);
    try {
      return clazz.getConstructor(Object.class, Object.class).newInstance(null, null);
    } catch (Exception e) {
      throw new ExtensionException("Failed to instantiate condition", e);
    }
  }

  private Object evaluateCondition(Closure condition) {
    return evaluateCondition(condition, emptyList(), null);
  }

  private Object evaluateCondition(Closure condition, List<String> dataVariables, Object[] dataValues) {
    PreconditionContext preconditionContext = new PreconditionContext();
    for (int i = 0, size = dataVariables.size(); i < size; i++) {
      preconditionContext.setDataVariable(dataVariables.get(i), dataValues[i]);
    }

    condition.setDelegate(preconditionContext);
    condition.setResolveStrategy(Closure.DELEGATE_ONLY);

    try {
      return condition.call();
    } catch (Exception e) {
      throw new ExtensionException("Failed to evaluate condition", e);
    }
  }
}
