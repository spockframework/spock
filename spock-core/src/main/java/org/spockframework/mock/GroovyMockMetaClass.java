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
import spock.mock.MockConfiguration;
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

    IMockInvocation invocation = createMockInvocation(metaMethod, target, method, arguments, isStatic, configuration.isGlobal());
    IMockInvocationMatcher invocationMatcher = specification.getSpecificationContext().getMockInvocationMatcher();

    // TODO: if global, need to devirtualize (invokeMethod("foo") -> foo(), etc.)
    InvocationMatchResult result = invocationMatcher.match(invocation);
    if (result.hasReturnValue()) return result.getReturnValue();

    if (configuration.isGlobal()) {
      // invoke original method
      if (isStatic && method.equals("<init>")) {
        return super.invokeConstructor(arguments);
      }
      if (isStatic) {
        return super.invokeStaticMethod(target, method, arguments);
      }
      return super.invokeMethod(target, method, arguments);
    }

    return DefaultStubInteractionScope.INSTANCE.match(invocation).accept(invocation);
  }

  private boolean isGetMetaClassCallOnGroovyObject(Object target, String method, Object[] arguments, boolean isStatic) {
    return !isStatic && target instanceof GroovyObject && method.equals("getMetaClass") && arguments.length == 0;
  }

  private IMockInvocation createMockInvocation(MetaMethod metaMethod, Object target,
      String method, Object[] arguments, boolean isStatic, boolean isGlobal) {
    IMockObject mockObject = new MockObject(configuration.getName(), configuration.getType(), target, configuration.isGlobal());
    IMockMethod mockMethod;
    if (metaMethod != null) {
      List<Class<?>> parameterTypes = Arrays.<Class<?>>asList(metaMethod.getNativeParameterTypes());
      mockMethod = new DynamicMockMethod(method, parameterTypes, metaMethod.getReturnType(), isStatic);
    } else {
      mockMethod = new DynamicMockMethod(method, arguments.length, isStatic);
    }
    if (isGlobal) {
      return new GlobalMockInvocation(mockObject, mockMethod, Arrays.asList(arguments));
    }
    return new MockInvocation(mockObject, mockMethod, Arrays.asList(arguments));
  }
}
