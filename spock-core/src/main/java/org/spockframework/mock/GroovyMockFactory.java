package org.spockframework.mock;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;

import org.spockframework.util.GroovyRuntimeUtil;

public class GroovyMockFactory implements IMockFactory {
  public Object create(MockSpec mockSpec, IInvocationDispatcher dispatcher) throws CannotCreateMockException {
    if (!mockSpec.getKind().equals("GroovyMock")) return null;

    GroovyMockOptions options = GroovyMockOptions.parse(mockSpec.getOptions());
    MetaClass oldMetaClass = GroovyRuntimeUtil.getMetaClass(mockSpec.getType());
    MetaClass newMetaClass = new MockMetaClass(oldMetaClass, mockSpec.getName(), mockSpec.getType(), dispatcher);

    IProxyBasedMockInterceptor mockInterceptor = new GroovyMockInterceptor(mockSpec, newMetaClass, dispatcher);
    Object proxy = new ProxyBasedMockFactory(mockInterceptor, options.isForceCglib()).create(mockSpec, dispatcher);

    if (!GroovyObject.class.isAssignableFrom(mockSpec.getType())) {
      GroovyRuntimeUtil.setMetaClass(proxy, newMetaClass);
    }

    return proxy;
  }
}
