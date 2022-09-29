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

package org.spockframework.spring;

import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.ErrorInfo;
import org.spockframework.util.NotThreadSafe;

@NotThreadSafe
public class SpringInterceptor extends AbstractMethodInterceptor {
  private final SpringTestContextManager manager;

  private Throwable exception;
  private boolean beforeTestMethodInvoked = false;

  public SpringInterceptor(SpringTestContextManager manager) {
    this.manager = manager;
  }

  @Override
  public void interceptSetupSpecMethod(IMethodInvocation invocation) throws Throwable {
    manager.beforeTestClass();
    if (invocation.getSpec().isAnnotationPresent(EnableSharedInjection.class)) {
      manager.prepareTestInstance(invocation.getInstance());
    }
    invocation.proceed();
  }

  @Override
  public void interceptInitializerMethod(IMethodInvocation invocation) throws Throwable {
    invocation.proceed(); // needs to run before so that mocks are already initialized
    manager.prepareTestInstance(invocation.getInstance());
  }

  @Override
  public void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
    exception = null;
    beforeTestMethodInvoked = true;
    manager.beforeTestMethod(invocation.getInstance(),
        invocation.getFeature().getFeatureMethod().getReflection());
    invocation.proceed();
  }

  @Override
  public void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
    if (!beforeTestMethodInvoked) {
      invocation.proceed();
      return;
    }
    beforeTestMethodInvoked = false;

    invocation.proceed();

    Throwable afterTestMethodEx = null;
    try {
      manager.afterTestMethod(invocation.getInstance(),
          invocation.getFeature().getFeatureMethod().getReflection(), exception);
    } catch (Throwable t) {
      afterTestMethodEx = t;
    }

    if (afterTestMethodEx != null) {
      if (exception == null) {
        throw afterTestMethodEx;
      } else {
        exception.addSuppressed(afterTestMethodEx);
      }
    }
  }

  @Override
  public void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
    invocation.proceed();

    Throwable afterTestClassEx = null;
    try {
      manager.afterTestClass();
    } catch (Throwable t) {
      afterTestClassEx = t;
    }

    if (afterTestClassEx != null) {
      if (exception == null) {
        throw afterTestClassEx;
      } else {
        exception.addSuppressed(afterTestClassEx);
      }
    }
  }

  public void error(ErrorInfo error) {
    if (exception == null)
      exception = error.getException();
  }
}
