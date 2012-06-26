package org.spockframework.mock;

import java.util.Collections;
import java.util.List;

import groovy.lang.GroovyInterceptable;
import groovy.lang.MetaClass;

import org.spockframework.util.GroovyRuntimeUtil;

public class DynamicMock implements GroovyInterceptable {
  private final String name;
  private final IInvocationDispatcher dispatcher;

  public DynamicMock(String name, IInvocationDispatcher dispatcher) {
    this.name = name;
    this.dispatcher = dispatcher;
  }

  public Object invokeMethod(String methodName, Object args) {
    IMockObject mockObject = new MockObject(name, null, this);
    List<Object> arguments = GroovyRuntimeUtil.asArgumentList(args);
    IMockMethod method = new DynamicMockMethod(methodName, arguments.size(), false);
    IMockInvocation invocation = new MockInvocation(mockObject, method, arguments);
    return dispatcher.dispatch(invocation);
  }

  public Object getProperty(String propertyName) {
    String methodName = GroovyRuntimeUtil.propertyToMethodName("get", propertyName);
    return invokeMethod(methodName, Collections.emptyList());
  }

  public void setProperty(String propertyName, Object newValue) {
    String methodName = GroovyRuntimeUtil.propertyToMethodName("set", propertyName);
    invokeMethod(methodName, Collections.singletonList(newValue));
  }

  public MetaClass getMetaClass() {
    return GroovyRuntimeUtil.getMetaClass(DynamicMock.class);
  }

  public void setMetaClass(MetaClass metaClass) {} // ignore
}
