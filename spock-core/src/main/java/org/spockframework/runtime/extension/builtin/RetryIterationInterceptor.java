/*
 * Copyright 2017 the original author or authors.
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

import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.MethodInfo;
import spock.lang.Retry;

import java.util.*;

import groovy.lang.Closure;
import org.opentest4j.MultipleFailuresError;

/**
 * @author Leonard Brünings
 * @since 1.2
 */
public class RetryIterationInterceptor extends RetryBaseInterceptor implements IMethodInterceptor {

  private boolean finalIteration;
  private final InnerRetryInterceptor retryInterceptor;
  private List<Throwable> throwables;

  public RetryIterationInterceptor(Retry retry, MethodInfo featureMethod) {
    super(retry);
    this.retryInterceptor = new InnerRetryInterceptor(retry, condition);
    featureMethod.addInterceptor(retryInterceptor);
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    throwables = new ArrayList<>();
    for (int i = 0; i <= retry.count(); i++) {
      finalIteration = i == retry.count();
      invocation.proceed();
      if (throwables.isEmpty()) {
        break;
      } else {
        if (retry.delay() > 0) Thread.sleep(retry.delay());
      }
    }
  }

  private class InnerRetryInterceptor extends RetryBaseInterceptor implements IMethodInterceptor {

    public InnerRetryInterceptor(Retry retry, Closure condition) {
      super(retry, condition);
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
      try {
        invocation.proceed();
        throwables.clear();
      } catch (Throwable e) {
        if (isExpected(invocation, e)) {
          throwables.add(e);
          if (finalIteration) {
            throw new MultipleFailuresError("Retries exhausted", throwables);
          }
        } else {
          throw e;
        }
      }
    }
  }
}
