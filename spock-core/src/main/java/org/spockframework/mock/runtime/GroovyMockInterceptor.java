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
import org.spockframework.runtime.GroovyRuntimeUtil;
import spock.lang.Specification;

import java.lang.reflect.Method;
import java.util.Arrays;

import groovy.lang.*;

public class GroovyMockInterceptor implements IProxyBasedMockInterceptor {
  private final IMockConfiguration mockConfiguration;
  private final Specification specification;
  private final MetaClass mockMetaClass;

  public GroovyMockInterceptor(IMockConfiguration mockConfiguration, Specification specification, MetaClass mockMetaClass) {
    this.mockConfiguration = mockConfiguration;
    this.specification = specification;
    this.mockMetaClass = mockMetaClass;
  }

  @Override
  public Object intercept(Object target, Method method, Object[] arguments, IResponseGenerator realMethodInvoker) {
    IMockObject mockObject = new MockObject(mockConfiguration.getName(), mockConfiguration.getExactType(), target,
        mockConfiguration.isVerified(), mockConfiguration.isGlobal(), mockConfiguration.getDefaultResponse(), specification, this);

    if (method.getDeclaringClass() == ISpockMockObject.class) {
      return mockObject;
    }

    // sometimes we see an argument wrapped in PojoWrapper
    // example is when GroovyObject.invokeMethod is invoked directly
    Object[] normalizedArgs = GroovyRuntimeUtil.asUnwrappedArgumentArray(arguments);

    if (isMethod(method, "getMetaClass")) {
      return mockMetaClass;
    }
    if (isMethod(method, "invokeMethod", String.class, Object.class)) {
      return GroovyRuntimeUtil.invokeMethod(target,
          (String) normalizedArgs[0], GroovyRuntimeUtil.asArgumentArray(normalizedArgs[1]));
    }
    if (isMethod(method, "getProperty", String.class)) {
      String methodName = GroovyRuntimeUtil.propertyToMethodName("get", (String) normalizedArgs[0]);
      return GroovyRuntimeUtil.invokeMethod(target, methodName);
    }
    if (isMethod(method, "setProperty", String.class, Object.class)) {
      String methodName = GroovyRuntimeUtil.propertyToMethodName("set", (String) normalizedArgs[0]);
      return GroovyRuntimeUtil.invokeMethod(target, methodName, normalizedArgs[1]);
    }
    if (isMethod(method, "methodMissing", String.class, Object.class)) {
      throw new MissingMethodException((String) normalizedArgs[0],
          mockConfiguration.getType(), new Object[] {normalizedArgs[1]}, false);
    }
    if (isMethod(method, "propertyMissing", String.class)) {
      throw new MissingPropertyException((String) normalizedArgs[0], mockConfiguration.getType());
    }

    IMockMethod mockMethod = new StaticMockMethod(method, mockConfiguration.getExactType());
    IMockInvocation invocation = new MockInvocation(mockObject, mockMethod, Arrays.asList(normalizedArgs), realMethodInvoker);
    IMockController controller = specification.getSpecificationContext().getMockController();

    return controller.handle(invocation);
  }

  private boolean isMethod(Method method, String name, Class<?>... parameterTypes) {
    return method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes);
  }

  @Override
  public void attach(Specification specification) {
    // NO-OP since GroovyMocks do not support detached mocks at the moment
  }

  @Override
  public void detach() {
    // NO-OP since GroovyMocks do not support detached mocks at the moment
  }
}
