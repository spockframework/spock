/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.mock.runtime;

import org.spockframework.mock.IResponseGenerator;
import org.spockframework.util.ReflectionUtil;

import java.lang.reflect.*;

public class DynamicProxyMockInterceptorAdapter implements InvocationHandler {
  private final IProxyBasedMockInterceptor interceptor;

  public DynamicProxyMockInterceptorAdapter(IProxyBasedMockInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  @Override
  public Object invoke(Object target, Method method, Object[] arguments) throws Throwable {
    IResponseGenerator realMethodInvoker = method.isDefault()
      ? new DefaultMethodInvoker(target, method, arguments)
      : ReflectionUtil.isObjectMethod(method)
      ? ObjectMethodInvoker.INSTANCE
      : new FailingRealMethodInvoker("Cannot invoke real method '" + method.getName() + "' on interface based mock object");
    return interceptor.intercept(target, method, arguments, realMethodInvoker);
  }
}
