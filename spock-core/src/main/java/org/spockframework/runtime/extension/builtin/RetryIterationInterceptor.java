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
import spock.lang.Retry;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import groovy.lang.Closure;
import org.opentest4j.MultipleFailuresError;

/**
 * @author Leonard Brünings
 * @since 1.2
 */
public class RetryIterationInterceptor extends RetryBaseInterceptor implements IMethodInterceptor {
  public RetryIterationInterceptor(Retry retry) {
    super(retry);
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    List<Throwable> throwableList = new ArrayList<>();
      Queue<Throwable> throwables = new ConcurrentLinkedQueue<>();
    for (int i = 0; i <= retry.count(); i++) {
      throwables.clear();
      invocation.getFeature().getFeatureMethod().addInterceptor(new InnerRetryInterceptor(retry, condition, throwables));
      invocation.proceed();
      if (throwables.isEmpty()) {
        throwableList.clear();
        break;
      } else {
        throwableList.addAll(throwables);
        if (retry.delay() > 0) Thread.sleep(retry.delay());
      }
    }
    if (!throwableList.isEmpty()) {
      throw new MultipleFailuresError("Retries exhausted", throwableList);
    }
  }

  static class InnerRetryInterceptor extends RetryBaseInterceptor implements IMethodInterceptor {

    private final Queue<Throwable> throwables;

    public InnerRetryInterceptor(Retry retry, Closure condition, Queue<Throwable> throwables) {
      super(retry, condition);
      this.throwables = throwables;
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
      try {
        invocation.proceed();
      } catch (Throwable e) {
        if (isExpected(invocation, e)) {
          throwables.add(e);
        } else {
          throw e;
        }
      }
    }
  }
}
