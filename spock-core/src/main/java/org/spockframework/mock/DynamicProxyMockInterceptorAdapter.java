/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.mock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DynamicProxyMockInterceptorAdapter implements InvocationHandler {
  private final IProxyBasedMockInterceptor interceptor;

  public DynamicProxyMockInterceptorAdapter(IProxyBasedMockInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  public Object invoke(Object target, Method method, Object[] arguments) throws Throwable {
    return interceptor.intercept(target, method, arguments,
        new UnsupportedRealMethodInvoker("Cannot invoke real method on interface based mock object"));
  }
}
