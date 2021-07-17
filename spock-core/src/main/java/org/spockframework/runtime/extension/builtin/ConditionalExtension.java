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
import org.spockframework.runtime.extension.ExtensionException;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.lang.annotation.Annotation;
import java.util.Map;

import static java.util.Collections.emptyMap;

public abstract class ConditionalExtension<T extends Annotation> implements IAnnotationDrivenExtension<T> {
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
    Object result = evaluateCondition(condition, spec.getReflection());
    specConditionResult(GroovyRuntimeUtil.isTruthy(result), annotation, spec);
  }

  @Override
  public void visitFeatureAnnotation(T annotation, FeatureInfo feature) {
    Closure condition = createCondition(annotation);

    try {
      Object result = evaluateCondition(condition, feature.getSpec().getReflection());
      featureConditionResult(GroovyRuntimeUtil.isTruthy(result), annotation, feature);
    } catch (ExtensionException ee) {
      if (!(ee.getCause() instanceof MissingPropertyException)) {
        throw ee;
      }
      MissingPropertyException mpe = (MissingPropertyException) ee.getCause();
      if (!"instance".equals(mpe.getProperty()) && !feature.getDataVariables().contains(mpe.getProperty())) {
        throw ee;
      }
      feature.getFeatureMethod().addInterceptor(new IterationCondition(condition, annotation));
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

  private static Object evaluateCondition(Closure condition, Object instance,
                                          Map<String, Object> dataVariables) {
    return evaluateCondition(condition, instance, dataVariables, null);
  }

  private static Object evaluateCondition(Closure condition, Object owner) {
    return evaluateCondition(condition, null, emptyMap(), owner);
  }

  private static Object evaluateCondition(Closure condition, Object instance,
                                          Map<String, Object> dataVariables, Object owner) {
    PreconditionContext context = new PreconditionContext(instance, dataVariables);
    condition = condition.rehydrate(context, owner, null);
    condition.setResolveStrategy(Closure.DELEGATE_FIRST);

    try {
      return condition.call(context);
    } catch (Exception e) {
      throw new ExtensionException("Failed to evaluate condition", e);
    }
  }

  private class IterationCondition implements IMethodInterceptor {
    private final Closure condition;
    private final T annotation;

    public IterationCondition(Closure condition, T annotation) {
      this.condition = condition;
      this.annotation = annotation;
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
      Object result = evaluateCondition(condition, invocation.getInstance(), invocation.getIteration().getDataVariables());
      iterationConditionResult(GroovyRuntimeUtil.isTruthy(result), annotation, invocation);
      invocation.proceed();
    }
  }
}
