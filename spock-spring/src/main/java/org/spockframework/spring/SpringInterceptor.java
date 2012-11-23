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
    invocation.proceed();
  }

  @Override
  public void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
    manager.prepareTestInstance(invocation.getInstance());
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
    
    Throwable cleanupEx = null;
    try {
      invocation.proceed();
    } catch (Throwable t) {
      cleanupEx = t;
      if (exception == null) exception = t;
    }

    Throwable afterTestMethodEx = null;
    try {
      manager.afterTestMethod(invocation.getInstance(),
          invocation.getFeature().getFeatureMethod().getReflection(), exception);
    } catch (Throwable t) {
      afterTestMethodEx = t;
    }
    
    if (cleanupEx != null) throw cleanupEx;
    if (afterTestMethodEx != null) throw afterTestMethodEx;
  }

  @Override
  public void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
    Throwable cleanupSpecEx = null;
    try {
      invocation.proceed();
    } catch (Throwable t) {
      cleanupSpecEx = t;
    }

    Throwable afterTestClassEx = null;
    try {
      manager.afterTestClass();
    } catch (Throwable t) {
      afterTestClassEx = t;
    }

    if (cleanupSpecEx != null) throw cleanupSpecEx;
    if (afterTestClassEx != null) throw afterTestClassEx;
  }

  public void error(ErrorInfo error) {
    if (exception == null)
      exception = error.getException();
  }
}
