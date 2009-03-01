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

package org.spockframework.runtime.intercept;

import spock.lang.Timeout;

import org.spockframework.runtime.SpeckAssertionError;

/**
 * Implementation of @Timeout.
 *
 * @author Peter Niederwieser
 */

public class TimeoutInterceptor implements IMethodInterceptor {
  private final Timeout timeout;

  public TimeoutInterceptor(Timeout timeout) {
    this.timeout = timeout;
  }

  public void invoke(final MethodInvocation invocation) throws Throwable {
    final Throwable[] exception = new Throwable[1];

    Thread thread = new Thread() {
      public void run() {
        try {
          invocation.proceed();
        } catch (Throwable t) {
          exception[0] = t;
        }
      }
    };

    thread.start();
    thread.join(timeout.unit().toMillis(timeout.value()));
    if (thread.isAlive()) {
      // IDEA: Isn't thread.stop() more likey to succeed (considering it throws
      // an Error instead of an Exception)? Are its risks tolerable here?
      thread.interrupt();
      throw new SpeckAssertionError("method timed out after %s %s", timeout.value(),
        timeout.unit().toString().toLowerCase());
    }

    if (exception[0] != null)
      throw exception[0];
  }
}