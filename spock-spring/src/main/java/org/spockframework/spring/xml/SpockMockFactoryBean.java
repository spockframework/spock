package org.spockframework.spring.xml;

import org.spockframework.mock.MockNature;
import spock.mock.DetachedMockFactory;

import java.util.Collections;

import org.springframework.beans.factory.FactoryBean;

/**
 * Takes care of instantiating detached spock Mocks.
 *
 * Spring integration of spock mocks is heavily inspired by
 * Springokito {@see https://bitbucket.org/kubek2k/springockito}.
 *
 * @author Leonard Bruenings
 */
public class SpockMockFactoryBean<T> implements FactoryBean<T> {

  private final Class<T> targetClass;
  private String name;
  private String mockNature = MockNature.MOCK.name();

  private T instance;

  public SpockMockFactoryBean (Class<T> targetClass) {
    this.targetClass = targetClass;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T getObject() throws Exception {
    if (instance == null) {
      MockNature nature = MockNature.valueOf(mockNature.toUpperCase());
      instance =  new DetachedMockFactory().createMock(name, targetClass, nature,
        Collections.<String, Object>emptyMap());
    }
    return instance;
  }

  @Override
  public Class<?> getObjectType() {
    return targetClass;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMockNature() {
    return mockNature;
  }

  public void setMockNature(String mockNature) {
    this.mockNature = mockNature;
  }
}
