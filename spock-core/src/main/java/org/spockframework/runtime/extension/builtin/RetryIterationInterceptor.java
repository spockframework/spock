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

import groovy.lang.Closure;
import org.opentest4j.MultipleFailuresError;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.extension.IStore.Namespace;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.util.ThreadSafe;
import spock.lang.Retry;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Leonard Brünings
 * @since 1.2
 */
public class RetryIterationInterceptor extends RetryBaseInterceptor implements IMethodInterceptor {
  private static final Namespace NAMESPACE = Namespace.create(RetryIterationInterceptor.class);
  private static final String ITERATION_STATE = "iterationState";

  public RetryIterationInterceptor(Retry retry, MethodInfo featureMethod) {
    super(retry);
    InnerRetryInterceptor interceptor = new InnerRetryInterceptor(retry, condition);
    addInterceptorToFeatureMethod(featureMethod, interceptor);
  }

  private static void addInterceptorToFeatureMethod(MethodInfo featureMethod, InnerRetryInterceptor interceptor) {
    List<IMethodInterceptor> interceptors = featureMethod.getInterceptors();
    for (int i = 0; i < interceptors.size(); i++) {
      IMethodInterceptor existing = interceptors.get(i);
      if (existing instanceof PendingFeatureInterceptor) {
        //Make sure we insert the RetryInterceptor before the PendingFeatureInterceptor
        //otherwise the PendingFeatureInterceptor will fail, because the RetryInterceptor swallowed the exceptions.
        //This would lead to a PendingFeatureSuccessfulError
        //TODO: Change this when https://github.com/spockframework/spock/issues/646 got implement to remove the hard-coded logic.
        interceptors.add(i, interceptor);
        return;
      }
    }
    interceptors.add(interceptor);
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    IterationState iterationState = new IterationState();
    invocation.getStore(NAMESPACE).put(ITERATION_STATE, iterationState);
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

    public InnerRetryInterceptor(Retry retry, Closure<?> condition) {
      super(retry, condition);
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
      IterationState iterationState = invocation.getStore(NAMESPACE).get(ITERATION_STATE);
      try {
        invocation.proceed();
        iterationState.resetFailures();
      } catch (Throwable e) {
        if (isExpected(invocation, e)) {
          iterationState.failIteration(e);
        } else {
          throw e;
        }
      }
    }
  }

  @ThreadSafe
  private class IterationState {
    private final AtomicBoolean finalIteration = new AtomicBoolean(false);
    private final List<Throwable> throwables = new CopyOnWriteArrayList<>();

    void resetFailures() {
      throwables.clear();
    }

    void setRetryAttempt(int retryAttempt) {
      finalIteration.set(retryAttempt == retry.count());
    }

    void failIteration(Throwable failure) {
      throwables.add(failure);
      if (finalIteration.get()) {
        throw new MultipleFailuresError("Retries exhausted", throwables);
      }
    }

    boolean notFailed() {
      return throwables.isEmpty();
    }
  }
}
