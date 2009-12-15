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

import org.springframework.test.annotation.ProfileValueUtils;
import org.springframework.test.context.TestContextManager;

import org.spockframework.runtime.SkipSpecOrFeatureException;
import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.FeatureInfo;

public class SpringInterceptor extends AbstractMethodInterceptor {
  private final TestContextManager manager;

  private FeatureInfo currentFeature;

  public SpringInterceptor(TestContextManager manager) {
    this.manager = manager;
  }

  @Override
  public void interceptSpecExecution(IMethodInvocation invocation) throws Throwable {
    if (!ProfileValueUtils.isTestEnabledInThisEnvironment(invocation.getTarget().getClass()))
      throw new SkipSpecOrFeatureException("Specification not enabled in this environment");
    invocation.proceed();
  }

  @Override
  public void interceptFeatureExecution(IMethodInvocation invocation) throws Throwable {
    currentFeature = invocation.getMethod().getFeature();
    try {
      if (!ProfileValueUtils.isTestEnabledInThisEnvironment(invocation.getTarget().getClass()))
        throw new SkipSpecOrFeatureException("Feature not enabled in this environment");
      invocation.proceed();
    } finally {
      currentFeature = null;
    }
  }

  @Override
  public void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
    manager.prepareTestInstance(invocation.getTarget());
    manager.beforeTestMethod(invocation.getTarget(), currentFeature.getFeatureMethod().getReflection());
    invocation.proceed();
  }

  @Override
  public void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
    Throwable cleanupEx = null;
    try {
      invocation.proceed();
    } catch (Throwable t) {
      cleanupEx = t;
    }

    Throwable afterTestEx = null;
    try {
      manager.afterTestMethod(invocation.getTarget(), currentFeature.getFeatureMethod().getReflection(), cleanupEx);
    } catch (Throwable t2) {
      afterTestEx = t2;
    }
    
    if (cleanupEx != null) throw cleanupEx;
    if (afterTestEx != null) throw afterTestEx;
  }
}
