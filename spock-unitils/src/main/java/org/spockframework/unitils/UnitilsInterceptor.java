/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.unitils;

import org.unitils.core.TestListener;
import org.unitils.core.Unitils;

import org.spockframework.runtime.extension.AbstractMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.util.NotThreadSafe;

@NotThreadSafe
public class UnitilsInterceptor extends AbstractMethodInterceptor {
  private final TestListener listener = Unitils.getInstance().getTestListener();
  private FeatureInfo currentFeature;

  @Override
  public void interceptSetupSpecMethod(IMethodInvocation invocation) throws Throwable {
    listener.beforeTestClass(invocation.getSharedInstance().getClass());
    invocation.proceed();
  }

  @Override
  public void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
    listener.afterCreateTestObject(invocation.getInstance());
    listener.beforeTestSetUp(invocation.getInstance(), currentFeature.getFeatureMethod().getReflection());
    invocation.proceed();
  }

  @Override
  public void interceptFeatureMethod(IMethodInvocation invocation) throws Throwable {
    Throwable throwable = null;
    listener.beforeTestMethod(invocation.getInstance(), invocation.getMethod().getReflection());
    try {
      invocation.proceed();
    } catch (Throwable t) {
      throwable = t;
      throw t;
    } finally {
      listener.afterTestMethod(invocation.getInstance(), invocation.getMethod().getReflection(), throwable);
    }
  }

  @Override
  public void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
    try {
      invocation.proceed();
    } finally {
      listener.afterTestTearDown(invocation.getInstance(), currentFeature.getFeatureMethod().getReflection());
    }
  }

  @Override
  public void interceptFeatureExecution(IMethodInvocation invocation) throws Throwable {
    currentFeature = invocation.getMethod().getFeature();
    try {
      invocation.proceed();
    } finally {
      currentFeature = null;
    }
  }
}
