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
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.MethodInfo;
import spock.lang.Retry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

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
    for (int i = 0; i <= retry.count(); i++) {
      iterationState.setRetryAttempt(invocation.getIteration(), i);
      invocation.proceed();
      if (iterationState.notFailed(invocation.getIteration())) {
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
        iterationState.resetFailures(invocation.getIteration());
      } catch (Throwable e) {
        if (isExpected(invocation, e)) {
          iterationState.failIteration(invocation.getIteration(), e);
        } else {
          throw e;
        }
      }
    }
  }

  private class IterationState {
    private final Map<IterationInfo, AtomicBoolean> finalIteration = new ConcurrentHashMap<>();
    private final Map<IterationInfo, List<Throwable>> throwables = new ConcurrentHashMap<>();

    void resetFailures(IterationInfo iteration) {
      this.throwables.computeIfPresent(iteration, (key, throwables) -> {
        throwables.clear();
        return throwables;
      });
    }

    void setRetryAttempt(IterationInfo iteration, int retryAttempt) {
      finalIteration
        .computeIfAbsent(iteration, key -> new AtomicBoolean(false))
        .set(retryAttempt == retry.count());
    }

    void failIteration(IterationInfo iteration, Throwable failure) {
      List<Throwable> throwables = this.throwables.computeIfAbsent(iteration, key -> new CopyOnWriteArrayList<>());
      throwables.add(failure);
      if (finalIteration.containsKey(iteration) && finalIteration.get(iteration).get()) {
        throw new MultipleFailuresError("Retries exhausted", throwables);
      }
    }

    boolean notFailed(IterationInfo iteration) {
      return !throwables.containsKey(iteration) || throwables.get(iteration).isEmpty();
    }
  }
}
