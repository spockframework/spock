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

public class JavaMockInterceptor extends BaseMockInterceptor {
  private final IMockConfiguration mockConfiguration;
  private Specification specification;
  private MockController fallbackMockController;

  public JavaMockInterceptor(IMockConfiguration mockConfiguration, Specification specification, MetaClass mockMetaClass) {
    super(mockMetaClass);
    this.mockConfiguration = mockConfiguration;
    this.specification = specification;
  }

  @Override
  public Object intercept(Object target, Method method, Object[] arguments, IResponseGenerator realMethodInvoker) {
    IMockObject mockObject = new MockObject(mockConfiguration.getName(), mockConfiguration.getExactType(),
      target, mockConfiguration.isVerified(), false, mockConfiguration.getDefaultResponse(), specification, this);

    if (method.getDeclaringClass() == ISpockMockObject.class) {
      return mockObject;
    }

    // here no instances of org.codehaus.groovy.runtime.wrappers.Wrapper subclasses
    // should arrive in the arguments array. If there are some found, it should first
    // be investigated whether they should have made it until here. If it is correct
    // that they arrived here, maybe GroovyRuntimeUtil.asUnwrappedArgumentArray needs
    // to be used to unwrap the arguments. Wrapper subclasses are used to transport
    // type cast information to select proper overloaded methods.
    Object[] args = GroovyRuntimeUtil.asArgumentArray(arguments);

    if (target instanceof GroovyObject) {
      if (isMethod(method, "getMetaClass")) {
        return getMockMetaClass();
      }
      if (isMethod(method, "setProperty", String.class, Object.class)) {
        Throwable throwable = new Throwable();
        StackTraceElement mockCaller = throwable.getStackTrace()[3];
        if ("org.codehaus.groovy.runtime.ScriptBytecodeAdapter".equals(mockCaller.getClassName())
          || "org.codehaus.groovy.runtime.InvokerHelper".equals(mockCaller.getClassName())) {
          // HACK: for some reason, runtime dispatches direct property access on mock classes via ScriptBytecodeAdapter
          // delegate to the corresponding setter method
          // for abstract groovy classes and interfaces it uses InvokerHelper
          String methodName = GroovyRuntimeUtil.propertyToMethodName("set", (String)args[0]);
          return GroovyRuntimeUtil.invokeMethod(target, methodName, GroovyRuntimeUtil.asArgumentArray(args[1]));
        }
      }
      if (isMethod(method, "getProperty", String.class)) {
        String methodName = handleGetProperty((GroovyObject)target, args);
        if (methodName != null) {
          return GroovyRuntimeUtil.invokeMethod(target, methodName);
        }
      }
    }

    IMockMethod mockMethod = new StaticMockMethod(method, mockConfiguration.getExactType());
    IMockInvocation invocation = new MockInvocation(mockObject, mockMethod, asList(args), realMethodInvoker);
    IMockController mockController = specification == null ? getFallbackMockController() :
      specification.getSpecificationContext().getMockController();

    return mockController.handle(invocation);
  }

  @Override
  public void attach(Specification specification) {
    this.specification = specification;

  }

  @Override
  public void detach() {
    this.specification = null;
  }

  public MockController getFallbackMockController() {
    if (fallbackMockController == null) {
      fallbackMockController = new MockController();
    }
    return fallbackMockController;
  }
}
