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

import spock.lang.FailsWith;

import org.spockframework.runtime.SpeckAssertionError;

/**
 *
 * @author Peter Niederwieser
 */
public class FailsWithInterceptor implements IMethodInterceptor {
  private final FailsWith failsWith;

  public FailsWithInterceptor(FailsWith failsWith) {
    this.failsWith = failsWith;
  }

  public void invoke(IMethodInvocation invocation) throws Throwable {
    try {
      invocation.proceed();
    } catch (Throwable t) {
      if (failsWith.value().isInstance(t)) return;
      error("got: " + t);
    }
    error("no exception was thrown");
  }                                           

  private void error(String msg) {
    throw new SpeckAssertionError("expected exception %s, but %s",
        failsWith.value().getName(), msg);
  }
}
