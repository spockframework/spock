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

import org.spockframework.runtime.SkipSpeckOrFeatureException;
import org.spockframework.runtime.intercept.IMethodInterceptor;
import org.spockframework.runtime.intercept.IMethodInvocation;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.util.Assert;
import org.spockframework.util.UnreachableCodeError;

public class SpringInterceptor implements IMethodInterceptor {
  private final TestContextManager manager;

  private FeatureInfo currentFeature;

  public SpringInterceptor(TestContextManager manager) {
    this.manager = manager;
  }

  public void invoke(IMethodInvocation invocation) throws Throwable {
    switch(invocation.getMethod().getKind()) {
      case SPECK_EXECUTION:
        Assert.notNull(invocation.getMethod().getReflection());
        
        if (!ProfileValueUtils.isTestEnabledInThisEnvironment(invocation.getTarget().getClass()))
          throw new SkipSpeckOrFeatureException("Specification not enabled in this environment");
        invocation.proceed();
        break;
      case FEATURE_EXECUTION:
        currentFeature = invocation.getMethod().getFeature();
        try {
        if (!ProfileValueUtils.isTestEnabledInThisEnvironment(invocation.getTarget().getClass()))
          throw new SkipSpeckOrFeatureException("Feature not enabled in this environment");
        invocation.proceed();
        } finally {
          currentFeature = null;
        }
        break;
      case SETUP:
        manager.prepareTestInstance(invocation.getTarget());
        manager.beforeTestMethod(invocation.getTarget(), currentFeature.getFeatureMethod().getReflection());
        invocation.proceed();
        break;
      case CLEANUP:
        Throwable throwable = null;
        try {
          invocation.proceed();
        } catch (Throwable t) {
          throwable = t;
        }
        Throwable throwable2 = null;
        try {
          manager.afterTestMethod(invocation.getTarget(), currentFeature.getFeatureMethod().getReflection(), throwable);
        } catch (Throwable t2) {
          throwable2 = t2;
        }
        if (throwable != null)
          throw throwable;
        if (throwable2 != null)
          throw throwable2;
        break;
      default:
        throw new UnreachableCodeError();
    }
  }
}
