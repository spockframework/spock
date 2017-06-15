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
import org.spockframework.util.ReflectionUtil;
import spock.lang.Specification;

import java.lang.reflect.*;
import java.util.*;

import groovy.lang.*;

public class GroovyMockMetaClass extends DelegatingMetaClass implements SpecificationAttachable {
  private final IMockConfiguration configuration;
  private final Specification specification;

  public GroovyMockMetaClass(IMockConfiguration configuration, Specification specification, MetaClass oldMetaClass) {
    super(oldMetaClass);
    this.configuration = configuration;
    this.specification = specification;
  }

  @Override
  public Object invokeMethod(Object target, String method, Object[] arguments) {
    return doInvokeMethod(target, method, arguments, false);
  }

  @Override
  public Object invokeStaticMethod(Object target, String method, Object[] arguments) {
    return doInvokeMethod(target, method, arguments, true);
  }

  @Override
  public Object invokeConstructor(Object[] arguments) {
    return doInvokeMethod(configuration.getType(), "<init>", arguments, true);
  }

  @Override
  public Object getProperty(Object target, String property) {
    String methodName = GroovyRuntimeUtil.propertyToMethodName("get", property);
    return invokeMethod(target, methodName, GroovyRuntimeUtil.EMPTY_ARGUMENTS);
  }

  @Override
  public void setProperty(Object target, String property, Object newValue) {
    String methodName = GroovyRuntimeUtil.propertyToMethodName("set", property);
    invokeMethod(target, methodName, new Object[] {newValue});
  }

  private Object doInvokeMethod(Object target, String methodName, Object[] arguments, boolean isStatic) {
    // get rid of PojoWrapper's; not sure where they come from, but they sometimes appear
    arguments = GroovyRuntimeUtil.asUnwrappedArgumentArray(arguments);

    if (isGetMetaClassCallOnGroovyObject(target, methodName, arguments, isStatic)) {
      // We handle this case explicitly because strangely enough, delegate.pickMethod()
      // selects DGM.getMetaClass() even for GroovyObject's. This would result in getMetaClass()
      // being mockable, but only Groovy callers would see the returned value (Groovy runtime would
      // always sees the MockMetaClass). Our handling leads to a more consistent behavior;
      // getMetaClass() returns same result no matter if it is invoked directly or via MOP.
      return ((GroovyObject) target).getMetaClass();
    }

    MetaMethod metaMethod = delegate.pickMethod(methodName, ReflectionUtil.getTypes(arguments));
    Method method = GroovyRuntimeUtil.toMethod(metaMethod);

    if (method != null && method.getDeclaringClass().isAssignableFrom(configuration.getType())) {
      if (!isStatic && !ReflectionUtil.isFinalMethod(method) && !configuration.isGlobal()) {
        // use standard proxy dispatch
        return metaMethod.invoke(target, arguments);
      }
    }

    // MetaMethod.getDeclaringClass apparently differs from java.reflect.Method.getDeclaringClass()
    // in that the originally declaring class/interface is returned; we leverage this behavior
    // to check if a GroovyObject method was called
    if (metaMethod != null && metaMethod.getDeclaringClass().getTheClass() == GroovyObject.class) {
      if ("invokeMethod".equals(methodName)) {
        return invokeMethod(target, (String) arguments[0], GroovyRuntimeUtil.asArgumentArray(arguments[1]));
      }
      if ("getProperty".equals(methodName)) {
        return getProperty(target, (String) arguments[0]);
      }
      if ("setProperty".equals(methodName)) {
        setProperty(target, (String) arguments[0], arguments[1]);
        return null;
      }
      // getMetaClass was already handled earlier; setMetaClass isn't handled specially
    }

    IMockInvocation invocation = createMockInvocation(metaMethod, target, methodName, arguments, isStatic);
    IMockController controller = specification.getSpecificationContext().getMockController();
    return controller.handle(invocation);
  }

  private boolean isGetMetaClassCallOnGroovyObject(Object target, String method, Object[] arguments, boolean isStatic) {
    return !isStatic && target instanceof GroovyObject && "getMetaClass".equals(method) && arguments.length == 0;
  }

  private IMockInvocation createMockInvocation(MetaMethod metaMethod, Object target,
      String methodName, Object[] arguments, boolean isStatic) {
    IMockObject mockObject = new MockObject(configuration.getName(), configuration.getExactType(), target,
        configuration.isVerified(), configuration.isGlobal(), configuration.getDefaultResponse(), specification, this);
    IMockMethod mockMethod;
    if (metaMethod != null) {
      List<Type> parameterTypes = Arrays.<Type>asList(metaMethod.getNativeParameterTypes());
      mockMethod = new DynamicMockMethod(methodName, parameterTypes, metaMethod.getReturnType(), isStatic);
    } else {
      mockMethod = new DynamicMockMethod(methodName, arguments.length, isStatic);
    }
    return new MockInvocation(mockObject, mockMethod, Arrays.asList(arguments), new GroovyRealMethodInvoker(getAdaptee()));
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
