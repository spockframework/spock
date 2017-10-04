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

import java.util.ArrayList;
import java.util.List;

import org.junit.runners.model.MultipleFailureException;
import org.spockframework.runtime.extension.IMethodInvocation;

import spock.lang.Retry;

/**
 * @author Leonard Brünings
 */
public class RetryBaseInterceptor {

  protected final Retry retry;

  public RetryBaseInterceptor(Retry retry) {
    this.retry = retry;
  }

  protected boolean isExpected(Throwable e) {
    for (Class<? extends Throwable> exception : retry.exceptions()) {
      if(exception.isInstance(e)) {
        return true;
      }
    }
    return false;
  }

  protected void handleInvocation(IMethodInvocation invocation) throws Throwable {
    List<Throwable> throwables = new ArrayList<>(retry.count() + 1);
    for (int i = 0; i <= retry.count(); i++) {
      try {
        invocation.proceed();
        return;
      } catch (Throwable e) {
        if (isExpected(e)) {
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
    throw new MultipleFailureException(throwables);
  }
}
