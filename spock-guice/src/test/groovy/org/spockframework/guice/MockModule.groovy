package org.spockframework.guice

import spock.mock.DetachedMockFactory

import com.google.inject.AbstractModule

class MockModule extends AbstractModule {
  @Override
  protected void configure() {
    DetachedMockFactory detachedMockFactory = new DetachedMockFactory()
    bind(IService1).toInstance(detachedMockFactory.Mock(IService1))
    bind(IService2).toInstance(detachedMockFactory.Stub(IService2))
  }
}
