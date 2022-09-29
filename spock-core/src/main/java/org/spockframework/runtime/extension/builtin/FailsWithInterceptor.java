/*
 * Copyright 2009 the original author or authors.
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

import org.spockframework.runtime.*;
import org.spockframework.runtime.extension.*;
import spock.lang.FailsWith;

/**
 *
 * @author Peter Niederwieser
 */
public class FailsWithInterceptor implements IMethodInterceptor {
  private final FailsWith failsWith;

  public FailsWithInterceptor(FailsWith failsWith) {
    this.failsWith = failsWith;
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    try {
      invocation.proceed();
    } catch (Throwable t) {
      if (checkException(t)) return;
      throw new WrongExceptionThrownError(failsWith.value(), t);
    }

    throw new WrongExceptionThrownError(failsWith.value(), null);
  }

  private boolean checkException(Throwable t) {
    return failsWith.value().isInstance(t)
      || (t instanceof ConditionFailedWithExceptionError && failsWith.value().isInstance(t.getCause()));
  }
}
