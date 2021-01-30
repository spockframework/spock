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

    // here no instances of org.codehaus.groovy.runtime.wrappers.Wrapper subclasses
    // should arrive in the arguments array. If there are some found, it should first
    // be investigated whether they should have made it until here. If it is correct
    // that they arrived here, maybe GroovyRuntimeUtil.asUnwrappedArgumentArray needs
    // to be used to unwrap the arguments. Wrapper subclasses are used to transport
    // type cast information to select proper overloaded methods.
    Object[] args = GroovyRuntimeUtil.asArgumentArray(arguments);

    if (target instanceof GroovyObject) {
      if (isMethod(method, "getMetaClass")) {
        return mockMetaClass;
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

      // Another hack It should be replaced with something more reliable: https://github.com/spockframework/spock/issues/1076
      if (isMethod(method, "getProperty", String.class)) {
        //Groovy 3 started to call go.getProperty("x") method instead of go.getX() directly for go.x
        Throwable throwable = new Throwable();
        StackTraceElement mockCaller = throwable.getStackTrace()[3];
        // In some strange cases the caller classname is `groovy.lang.GroovyObject$getProperty$0` so we must use starts with here
        if (!(mockCaller.getClassName().startsWith("groovy.lang.GroovyObject$getProperty") && "call".equals(mockCaller.getMethodName()))) {
          //HACK: Only explicit getter executions (go.foo and go.getFoo()) should be deeper processed.
          //go.getProperty("foo") is treated as is (to allow for its stubbing)
          String propertyName = (String)args[0];
          MetaClass metaClass = ((GroovyObject)target).getMetaClass();
          String methodName = GroovyRuntimeUtil.propertyToMethodName("get", propertyName);
          MetaMethod metaMethod = metaClass.getMetaMethod(methodName, GroovyRuntimeUtil.EMPTY_ARGUMENTS);
          if (metaMethod == null) {
            MetaMethod booleanVariant = metaClass
              .getMetaMethod(GroovyRuntimeUtil.propertyToMethodName("is", propertyName), GroovyRuntimeUtil.EMPTY_ARGUMENTS);
            if (booleanVariant != null && booleanVariant.getReturnType() == boolean.class) {
              methodName = booleanVariant.getName();
            }
          }
          return GroovyRuntimeUtil.invokeMethod(target, methodName);
        }
      }
    }

    IMockMethod mockMethod = new StaticMockMethod(method, mockConfiguration.getExactType());
    IMockInvocation invocation = new MockInvocation(mockObject, mockMethod, Arrays.asList(args), realMethodInvoker);
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
