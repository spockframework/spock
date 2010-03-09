/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.spring;

import java.lang.reflect.Method;

import org.springframework.test.context.TestContextManager;

import org.spockframework.util.Util;

/**
 * Wrapper around Spring's TestContextManager class that works with Spring 2.5 and Spring 3.
 */
public class SpringTestContextManager {
  private static final Method beforeTestClassMethod =
      Util.getMethodBySignature(TestContextManager.class, "beforeTestClass");
  private static final Method afterTestClassMethod =
      Util.getMethodBySignature(TestContextManager.class, "afterTestClass");

  private final TestContextManager delegate;

  public SpringTestContextManager(Class<?> testClass) {
    delegate = new TestContextManager(testClass);
  }

  public void beforeTestClass() throws Exception {
    if (beforeTestClassMethod != null)
      Util.invokeMethodThatThrowsException(delegate, beforeTestClassMethod);
  }

  public void afterTestClass() throws Exception {
    if (afterTestClassMethod != null)
      Util.invokeMethodThatThrowsException(delegate, afterTestClassMethod);
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
