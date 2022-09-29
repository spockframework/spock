/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.runtime.extension.*;
import spock.lang.Retry;

import java.util.*;

import groovy.lang.Closure;
import org.opentest4j.MultipleFailuresError;

/**
 * @author Leonard Brünings
 */
public class RetryBaseInterceptor {

  protected final Retry retry;
  protected final Closure condition;

  public RetryBaseInterceptor(Retry retry) {
    this(retry, createCondition(retry.condition()));
  }

  protected RetryBaseInterceptor(Retry retry, Closure condition) {
    this.retry = retry;
    this.condition = condition;
  }

  private static Closure createCondition(Class<? extends Closure> clazz) {
    if (clazz.equals(Closure.class)) {
      return null;
    }
    try {
      return clazz.getConstructor(Object.class, Object.class).newInstance(null, null);
    } catch (Exception e) {
      throw new ExtensionException("Failed to instantiate @Retry condition", e);
    }
  }

  protected boolean isExpected(IMethodInvocation invocation, Throwable failure) {
    return hasExpectedClass(failure) && satisfiesCondition(invocation, failure);
  }

  private boolean hasExpectedClass(Throwable failure) {
    for (Class<? extends Throwable> exception : retry.exceptions()) {
      if (exception.isInstance(failure)) {
        return true;
      }
    }
    return false;
  }

  private boolean satisfiesCondition(IMethodInvocation invocation, Throwable failure) {
    if (condition == null) {
      return true;
    }
    final Closure condition = this.condition.rehydrate(
      new RetryConditionContext(invocation.getInstance(), failure),
      invocation.getSpec().getReflection(),
      null);
    condition.setResolveStrategy(Closure.DELEGATE_FIRST);

    try {
      return GroovyRuntimeUtil.isTruthy(condition.call());
    } catch (Exception e) {
      throw new ExtensionException("Failed to evaluate @Retry condition", e);
    }
  }

  protected void handleInvocation(IMethodInvocation invocation) throws Throwable {
    List<Throwable> throwables = new ArrayList<>(retry.count() + 1);
    for (int i = 0; i <= retry.count(); i++) {
      try {
        invocation.proceed();
        return;
      } catch (Throwable e) {
        if (isExpected(invocation, e)) {
          throwables.add(e);
          if (retry.delay() > 0) {
            Thread.sleep(retry.delay());
          }
          continue;
        } else {
          throw e;
        }
      }
    }
    throw new MultipleFailuresError("Retries exhausted", throwables);
  }
}
