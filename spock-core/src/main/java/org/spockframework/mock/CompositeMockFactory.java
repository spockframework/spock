package org.spockframework.mock;

import org.spockframework.util.CollectionUtil;
import org.spockframework.util.InternalSpockError;
import spock.lang.Specification;
import spock.mock.MockConfiguration;

import java.util.Map;

public class CompositeMockFactory implements IMockFactory {
  public static CompositeMockFactory INSTANCE =
      new CompositeMockFactory(CollectionUtil.mapOf("java", JavaMockFactory.INSTANCE, "groovy", GroovyMockFactory.INSTANCE));

  private final Map<String, IMockFactory> mockFactories;

  public CompositeMockFactory(Map<String, IMockFactory> mockFactories) {
    this.mockFactories = mockFactories;
  }

  public Object create(MockConfiguration configuration, Specification specification) {
    IMockFactory factory = mockFactories.get(configuration.getImpl());
    if (factory == null) {
      throw new InternalSpockError("No mock factory for implementation '%s' registered").withArgs(configuration.getImpl());
    }
    return factory.create(configuration, specification);
  }
}
