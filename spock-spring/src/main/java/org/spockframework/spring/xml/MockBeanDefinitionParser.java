package org.spockframework.spring.xml;

import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * Adds support for the spock:mock element.
 *
 * Spring integration of spock mocks is heavily inspired by
 * Springokito {@see https://bitbucket.org/kubek2k/springockito}.
 *
 * @author Leonard Bruenings
 */
public class MockBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

  @Override
  protected Class<?> getBeanClass(Element element) {
    return SpockMockFactoryBean.class;
  }

  @Override
  protected String getBeanClassName(Element element) {
    return getBeanClass(element).getName();
  }

  @Override
  protected void doParse(Element element, BeanDefinitionBuilder builder) {

    // configure MockFactory
    builder.addConstructorArgValue(element.getAttribute("class"));
    builder.addPropertyValue("name", element.getAttribute("id"));
    builder.addPropertyValue("mockNature", element.getLocalName());
  }

  @Override
  protected void postProcessComponentDefinition(BeanComponentDefinition componentDefinition) {
    super.postProcessComponentDefinition(componentDefinition);
    componentDefinition.getBeanDefinition().setPrimary(true);
  }

}
