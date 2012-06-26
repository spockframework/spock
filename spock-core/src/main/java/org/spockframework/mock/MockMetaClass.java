package org.spockframework.mock;

import java.util.Arrays;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;

import org.spockframework.util.GroovyRuntimeUtil;
import org.spockframework.util.ReflectionUtil;

public class MockMetaClass extends DelegatingMetaClass {
  private final String mockName;
  private final Class<?> mockType;
  private final IInvocationDispatcher dispatcher;

  public MockMetaClass(MetaClass delegate, String mockName, Class<?> mockType, IInvocationDispatcher dispatcher) {
    super(delegate);
    this.mockName = mockName;
    this.mockType = mockType;
    this.dispatcher = dispatcher;
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
      // always sees the MockMetaClass). The explicit handling leads to a more consistent behavior
      // (getMetaClass() returns same result no matter if it is invoked directly or via MOP).
      return ((GroovyObject) target).getMetaClass();
    }
    MetaMethod metaMethod = delegate.pickMethod(method, ReflectionUtil.getTypes(arguments));
    if (GroovyRuntimeUtil.isPhysicalMethod(metaMethod, mockType)) {
      return metaMethod.invoke(target, arguments);
    }
    IMockObject mockObject = new MockObject(mockName, mockType, target);
    IMockMethod mockMethod = new DynamicMockMethod(method, arguments.length, isStatic);
    IMockInvocation invocation = new MockInvocation(mockObject, mockMethod, Arrays.asList(arguments));
    return dispatcher.dispatch(invocation);
  }

  private boolean isGetMetaClassCallOnGroovyObject(Object target, String method, Object[] arguments, boolean isStatic) {
    return target instanceof GroovyObject && method.equals("getMetaClass") && arguments.length == 0 && !isStatic;
  }
}
