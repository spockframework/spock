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

  private final IterationState iterationState = new IterationState();

  public RetryIterationInterceptor(Retry retry, MethodInfo featureMethod) {
    super(retry);
    featureMethod.addInterceptor(new InnerRetryInterceptor(retry, condition));
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    iterationState.startIteration();
    for (int i = 0; i <= retry.count(); i++) {
      iterationState.setRetryAttempt(i);
      invocation.proceed();
      if (iterationState.notFailed()) {
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
        iterationState.startIteration();
      } catch (Throwable e) {
        if (isExpected(invocation, e)) {
          iterationState.failIteration(e);
        } else {
          throw e;
        }
      }
    }
  }

  private class IterationState {
    private final ThreadLocal<Boolean> finalIteration = ThreadLocal.withInitial(() -> false);
    private final ThreadLocal<List<Throwable>> throwables = new ThreadLocal<>();

    void startIteration() {
      throwables.set(new ArrayList<>());
    }

    void setRetryAttempt(int retryAttempt) {
      finalIteration.set(retryAttempt == retry.count());
    }

    void failIteration(Throwable failure) {
      throwables.get().add(failure);
      if (finalIteration.get()) {
        throw new MultipleFailuresError("Retries exhausted", throwables.get());
      }
    }

    boolean notFailed() {
      return throwables.get().isEmpty();
    }
  }
}
