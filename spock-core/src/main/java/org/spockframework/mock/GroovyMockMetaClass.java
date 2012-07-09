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

import java.util.Arrays;
import java.util.List;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;

import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.util.ReflectionUtil;

import spock.lang.Specification;
import spock.mock.MockConfiguration;

public class GroovyMockMetaClass extends DelegatingMetaClass {
  private final MockConfiguration configuration;
  private final Specification specification;

  public GroovyMockMetaClass(MockConfiguration configuration, Specification specification, MetaClass oldMetaClass) {
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

  private Object doInvokeMethod(Object target, String method, Object[] arguments, boolean isStatic) {
    if (isGetMetaClassCallOnGroovyObject(target, method, arguments, isStatic)) {
      // We handle this case explicitly because strangely enough, delegate.pickMethod()
      // selects DGM.getMetaClass() even for GroovyObject's. This would result in getMetaClass()
      // being mockable, but only Groovy callers would see the returned value (Groovy runtime would
      // always sees the MockMetaClass). Our handling leads to a more consistent behavior;
      // getMetaClass() returns same result no matter if it is invoked directly or via MOP.
      return ((GroovyObject) target).getMetaClass();
    }

    MetaMethod metaMethod = delegate.pickMethod(method, ReflectionUtil.getTypes(arguments));
    if (!isStatic && !configuration.isGlobal()
        && GroovyRuntimeUtil.isPhysicalMethod(metaMethod, configuration.getType())) {
      return metaMethod.invoke(target, arguments);
    }

    IMockInvocation invocation = createMockInvocation(metaMethod, target, method, arguments, isStatic);
    IMockController controller = specification.getSpecificationContext().getMockController();

    // TODO: if global, need to devirtualize (invokeMethod("foo") -> foo(), etc.)
    return controller.handle(invocation);
  }

  private boolean isGetMetaClassCallOnGroovyObject(Object target, String method, Object[] arguments, boolean isStatic) {
    return !isStatic && target instanceof GroovyObject && method.equals("getMetaClass") && arguments.length == 0;
  }

  private IMockInvocation createMockInvocation(MetaMethod metaMethod, Object target,
      String method, Object[] arguments, boolean isStatic) {
    IMockObject mockObject = new MockObject(configuration.getName(), configuration.getType(), target,
        configuration.isVerified(), configuration.isGlobal(), configuration.getDefaultResponse());
    IMockMethod mockMethod;
    if (metaMethod != null) {
      List<Class<?>> parameterTypes = Arrays.<Class<?>>asList(metaMethod.getNativeParameterTypes());
      mockMethod = new DynamicMockMethod(method, parameterTypes, metaMethod.getReturnType(), isStatic);
    } else {
      mockMethod = new DynamicMockMethod(method, arguments.length, isStatic);
    }
    return new MockInvocation(mockObject, mockMethod, Arrays.asList(arguments), new GroovyRealMethodInvoker(getAdaptee()));
  }
}
