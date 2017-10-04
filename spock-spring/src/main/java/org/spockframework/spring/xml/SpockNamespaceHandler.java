package org.spockframework.spring.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Adds support for the spock namespace.
 *
 * Spring integration of spock mocks is heavily inspired by
 * Springokito {@see https://bitbucket.org/kubek2k/springockito}.
 *
 * @author Leonard Bruenings
 */
public class SpockNamespaceHandler extends NamespaceHandlerSupport {

  @Override
  public void init() {
    registerBeanDefinitionParser("mock", new MockBeanDefinitionParser());
    registerBeanDefinitionParser("spy", new MockBeanDefinitionParser());
    registerBeanDefinitionParser("stub", new MockBeanDefinitionParser());
    registerBeanDefinitionParser("wrapWithSpy", new WrapWithSpyBeanDefinitionParser());
  }
}
