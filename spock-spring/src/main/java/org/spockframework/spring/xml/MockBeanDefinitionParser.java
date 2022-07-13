/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
