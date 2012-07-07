package org.spockframework.mock;

import java.lang.reflect.Modifier;
import java.util.Collections;

import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;

import org.spockframework.runtime.GroovyRuntimeUtil;

import spock.lang.Specification;
import spock.mock.MockConfiguration;

public class GroovyMockFactory implements IMockFactory {
  public static GroovyMockFactory INSTANCE = new GroovyMockFactory();

  public Object create(MockConfiguration configuration, Specification specification) throws CannotCreateMockException {
    final MetaClass oldMetaClass = GroovyRuntimeUtil.getMetaClass(configuration.getType());
    GroovyMockMetaClass newMetaClass = new GroovyMockMetaClass(configuration, specification, oldMetaClass);
    final Class<?> type = configuration.getType();

    if (configuration.isGlobal()) {
      if (type.isInterface()) {
        throw new CannotCreateMockException(type,
            ". Global mocking is only possible for classes, but not for interfaces.");
      }
      GroovyRuntimeUtil.setMetaClass(type, newMetaClass);
      specification.getSpecificationContext().getIterationInfo().addCleanup(new Runnable() {
        public void run() {
          GroovyRuntimeUtil.setMetaClass(type, oldMetaClass);
        }
      });
      return MockInstantiator.instantiate(type, type);
    }

    if (isFinalClass(type)) {
      final Object instance = MockInstantiator.instantiate(type, type);
      GroovyRuntimeUtil.setMetaClass(instance, newMetaClass);

      return instance;
    }

    IProxyBasedMockInterceptor mockInterceptor = new GroovyMockInterceptor(configuration, specification, newMetaClass);
    return ProxyBasedMockFactory.INSTANCE.create(type,
        Collections.<Class<?>>singletonList(GroovyObject.class), mockInterceptor, specification);
  }

  private boolean isFinalClass(Class<?> type) {
    return !type.isInterface() && Modifier.isFinal(type.getModifiers());
  }
}
