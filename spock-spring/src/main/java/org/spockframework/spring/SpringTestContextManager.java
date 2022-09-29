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

import org.spockframework.util.ReflectionUtil;

import java.lang.reflect.Method;

import org.springframework.test.context.TestContextManager;

/**
 * Wrapper around Spring's TestContextManager class that works with Spring 2.5 and Spring 3.
 */
public class SpringTestContextManager {
  private static final Method beforeTestClassMethod =
      ReflectionUtil.getMethodBySignature(TestContextManager.class, "beforeTestClass");
  private static final Method afterTestClassMethod =
      ReflectionUtil.getMethodBySignature(TestContextManager.class, "afterTestClass");
  private static final boolean testContextBootstrapperAvailable =
    ReflectionUtil.isClassAvailable("org.springframework.test.context.support.DefaultTestContextBootstrapper");

  private final TestContextManager delegate;

  public SpringTestContextManager(Class<?> testClass) {
    delegate = new TestContextManager(testClass);
    if (!testContextBootstrapperAvailable) {
      delegate.registerTestExecutionListeners(new SpringMockTestExecutionListener());
    }
  }

  public void beforeTestClass() throws Exception {
    if (beforeTestClassMethod != null)
      ReflectionUtil.invokeMethod(delegate, beforeTestClassMethod);
  }

  public void afterTestClass() throws Exception {
    if (afterTestClassMethod != null)
      ReflectionUtil.invokeMethod(delegate, afterTestClassMethod);
  }

  public void prepareTestInstance(Object testInstance) throws Exception {
    delegate.prepareTestInstance(testInstance);
  }

  public void beforeTestMethod(Object testInstance, Method testMethod) throws Exception {
    delegate.beforeTestMethod(testInstance, testMethod);
  }

  public void afterTestMethod(Object testInstance, Method testMethod, Throwable exception) throws Exception {
    delegate.afterTestMethod(testInstance, testMethod, exception);
  }
}
