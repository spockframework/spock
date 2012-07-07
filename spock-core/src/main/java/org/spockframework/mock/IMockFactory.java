package org.spockframework.mock;

import spock.lang.Specification;
import spock.mock.MockConfiguration;

public interface IMockFactory {
  public Object create(MockConfiguration configuration, Specification specification);
}
