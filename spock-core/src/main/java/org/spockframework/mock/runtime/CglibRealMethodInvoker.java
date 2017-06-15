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

package org.spockframework.mock.runtime;

import org.spockframework.mock.*;
import org.spockframework.util.ExceptionUtil;

import net.sf.cglib.proxy.MethodProxy;

public class CglibRealMethodInvoker implements IResponseGenerator {
  private final MethodProxy methodProxy;

  public CglibRealMethodInvoker(MethodProxy methodProxy) {
    this.methodProxy = methodProxy;
  }

  @Override
  public Object respond(IMockInvocation invocation) {
    try {
      if(invocation.getMockObject().getUserCreatedInstance() != null) {
        return methodProxy.invoke(invocation.getMockObject().getUserCreatedInstance(), invocation.getArguments().toArray());
      } else {
        return methodProxy.invokeSuper(invocation.getMockObject().getInstance(), invocation.getArguments().toArray());
      }
    } catch (Throwable t) {
      // MethodProxy doesn't wrap exceptions in InvocationTargetException, so no need to unwrap
      ExceptionUtil.sneakyThrow(t);
      return null; // unreachable
    }
  }
}
