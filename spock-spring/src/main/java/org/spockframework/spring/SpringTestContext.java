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

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;

/**
 * Wrapper around Spring's TestContext class that works with Spring 2.5 and Spring 3.
 */
public class SpringTestContext {
  private static final Method getApplicationContextMethod =
    ReflectionUtil.getMethodBySignature(TestContext.class, "getApplicationContext");
  private static final Method getTestInstanceMethod =
    ReflectionUtil.getMethodBySignature(TestContext.class, "getTestInstance");
  private static final Method setAttributeMethod =
    ReflectionUtil.getMethodBySignature(TestContext.class, "setAttribute", String.class, Object.class);
  private static final Method getAttributeMethod =
    ReflectionUtil.getMethodBySignature(TestContext.class, "getAttribute", String.class);

  private final TestContext delegate;

  public SpringTestContext(TestContext testContext) {
    delegate = testContext;
  }

  public ApplicationContext getApplicationContext() {
    if (getApplicationContextMethod == null) {
      throw new SpringExtensionException("Method 'TestContext.getApplicationContext()' was not found");
    }
    return (ApplicationContext) ReflectionUtil.invokeMethod(delegate, getApplicationContextMethod);
  }

  public Object getTestInstance() {
    if (getTestInstanceMethod == null) {
      throw new SpringExtensionException("Method 'TestContext.getTestInstance()' was not found");
    }
    return ReflectionUtil.invokeMethod(delegate, getTestInstanceMethod);
  }

  public void setAttribute(String name, Object value) {
    if (setAttributeMethod == null) {
      throw new SpringExtensionException("Method 'TestContext.setAttribute()' was not found");
    }
    ReflectionUtil.invokeMethod(delegate, setAttributeMethod, name, value);
  }

  public Object getAttribute(String name) {
    if (getAttributeMethod == null) {
      throw new SpringExtensionException("Method 'TestContext.getAttribute()' was not found");
    }
    return ReflectionUtil.invokeMethod(delegate, getAttributeMethod, name);
  }
}
