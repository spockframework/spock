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

public class JavaMockInterceptor implements IProxyBasedMockInterceptor {
  private final IMockConfiguration mockConfiguration;
  private Specification specification;
  private MockController fallbackMockController;
  private final MetaClass mockMetaClass;

  public JavaMockInterceptor(IMockConfiguration mockConfiguration, Specification specification, MetaClass mockMetaClass) {
    this.mockConfiguration = mockConfiguration;
    this.specification = specification;
    this.mockMetaClass = mockMetaClass;
  }

  @Override
  public Object intercept(Object target, Method method, Object[] arguments, IResponseGenerator realMethodInvoker) {
    IMockObject mockObject = new MockObject(mockConfiguration.getName(), mockConfiguration.getExactType(),
        target, mockConfiguration.isVerified(), false, mockConfiguration.getDefaultResponse(), specification, this);

    if (method.getDeclaringClass() == ISpockMockObject.class) {
      return mockObject;
    }

    // sometimes we see an argument wrapped in PojoWrapper
    // example is when GroovyObject.invokeMethod is invoked directly
    Object[] normalizedArgs = GroovyRuntimeUtil.asUnwrappedArgumentArray(arguments);

    if (target instanceof GroovyObject) {
      if (isMethod(method, "getMetaClass")) {
        return mockMetaClass;
      }
      if (isMethod(method, "setProperty", String.class, Object.class)) {
        Throwable throwable = new Throwable();
        StackTraceElement mockCaller = throwable.getStackTrace()[3];
        if ("org.codehaus.groovy.runtime.ScriptBytecodeAdapter".equals(mockCaller.getClassName())) {
          // for some reason, runtime dispatches direct property access on mock classes via ScriptBytecodeAdapter
          // delegate to the corresponding setter method
          String methodName = GroovyRuntimeUtil.propertyToMethodName("set", (String) normalizedArgs[0]);
          return GroovyRuntimeUtil.invokeMethod(target, methodName, GroovyRuntimeUtil.asArgumentArray(normalizedArgs[1]));
        }
      }
    }

    IMockMethod mockMethod = new StaticMockMethod(method, mockConfiguration.getExactType());
    IMockInvocation invocation = new MockInvocation(mockObject, mockMethod, Arrays.asList(normalizedArgs), realMethodInvoker);
    IMockController mockController = specification == null ? getFallbackMockController() :
                                                             specification.getSpecificationContext().getMockController();

    return mockController.handle(invocation);
  }

  private boolean isMethod(Method method, String name, Class<?>... parameterTypes) {
    return method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parameterTypes);
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

