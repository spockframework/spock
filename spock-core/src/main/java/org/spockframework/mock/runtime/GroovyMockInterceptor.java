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

import org.spockframework.mock.*;
import org.spockframework.runtime.GroovyRuntimeUtil;
import spock.lang.Specification;

import java.lang.reflect.Method;

import groovy.lang.*;

import static java.util.Arrays.asList;

public class GroovyMockInterceptor extends BaseMockInterceptor {
  private final IMockConfiguration mockConfiguration;
  private final Specification specification;

  public GroovyMockInterceptor(IMockConfiguration mockConfiguration, Specification specification, MetaClass mockMetaClass) {
    super(mockMetaClass);
    this.mockConfiguration = mockConfiguration;
    this.specification = specification;
  }

  @Override
  public Object intercept(Object target, Method method, Object[] arguments, IResponseGenerator realMethodInvoker) {
    IMockObject mockObject = new MockObject(mockConfiguration, target, specification, this);

    if (method.getDeclaringClass() == ISpockMockObject.class) {
      return handleSpockMockInterface(method, mockObject);
    }

    // we do not need the cast information from the wrappers here, the method selection
    // is already done, so unwrap the argument array if there are still wrappers present
    Object[] args = GroovyRuntimeUtil.asUnwrappedArgumentArray(arguments);

    if (isMethod(method, "getMetaClass")) {
      return getMockMetaClass();
    }
    if (isMethod(method, "invokeMethod", String.class, Object.class)) {
      return GroovyRuntimeUtil.invokeMethod(target,
        (String)args[0], GroovyRuntimeUtil.asArgumentArray(args[1]));
    }
    if (isMethod(method, "getProperty", String.class)) {
      String methodName = handleGetProperty((GroovyObject)target, args);
      if (methodName != null) {
        return GroovyRuntimeUtil.invokeMethod(target, methodName);
      }
    }
    if (isMethod(method, "setProperty", String.class, Object.class)) {
      String methodName = GroovyRuntimeUtil.propertyToSetterMethodName((String) args[0]);
      return GroovyRuntimeUtil.invokeMethod(target, methodName, args[1]);
    }
    if (isMethod(method, "methodMissing", String.class, Object.class)) {
      throw new MissingMethodException((String)args[0],
        mockConfiguration.getType(), new Object[]{args[1]}, false);
    }
   
    IMockMethod mockMethod = new StaticMockMethod(method, mockConfiguration.getExactType());
    IMockInvocation invocation = new MockInvocation(mockObject, mockMethod, asList(args), realMethodInvoker);
    IMockController controller = specification.getSpecificationContext().getMockController();

    return controller.handle(invocation);
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
