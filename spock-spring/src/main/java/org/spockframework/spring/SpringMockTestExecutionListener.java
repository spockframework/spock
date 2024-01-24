/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.spring;

import org.spockframework.mock.MockUtil;
import org.spockframework.spring.mock.SpockMockPostprocessor;
import org.spockframework.util.ReflectionUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import spock.lang.Specification;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

/**
 * This {@link TestExecutionListener} takes care of attaching and detaching the
 * mocks to the current {@link Specification}.
 *
 * @author Leonard Bruenings
 */
public class SpringMockTestExecutionListener extends AbstractSpringTestExecutionListener implements Ordered {

  public static final String MOCKED_BEANS_LIST = "org.spockframework.spring.SpringMockTestExecutionListener.MOCKED_BEANS_LIST";

  private final MockUtil mockUtil = new MockUtil();

  @Override
  public int getOrder() {
    // needs to run before the DependencyInjectionTestExecutionListener
    // https://github.com/spockframework/spock/issues/1790
    return 1955;
  }

  @Override
  public void beforeTestClass(SpringTestContext testContext) throws Exception {
  }

  @Override
  public void prepareTestInstance(SpringTestContext testContext) throws Exception {
    Object testInstance = testContext.getTestInstance();
    if (!(testInstance instanceof Specification)) return;

    injectSpies(testContext);
  }

  private static void injectSpies(SpringTestContext testContext) {
    ApplicationContext applicationContext = testContext.getApplicationContext();
    if (applicationContext.containsBean(SpockMockPostprocessor.class.getName())) {
      SpockMockPostprocessor mockPostprocessor = applicationContext.getBean(SpockMockPostprocessor.class);
      mockPostprocessor.injectSpies(testContext.getTestInstance());
    }
  }

  @Override
  public void beforeTestMethod(SpringTestContext testContext) throws Exception {
    Object testInstance = testContext.getTestInstance();
    if (!(testInstance instanceof Specification)) return;

    if (Boolean.TRUE.equals(testContext.getAttribute(
      DependencyInjectionTestExecutionListener.REINJECT_DEPENDENCIES_ATTRIBUTE))) {
      // we need to reinject the spies https://github.com/spockframework/spock/issues/1790
      injectSpies(testContext);
    }

    Specification specification = (Specification)testInstance;
    ScanScopedBeans scanScopedBeans = ReflectionUtil.getAnnotationRecursive(specification.getClass(), ScanScopedBeans.class);
    Set<String> scopes = scanScopedBeans == null
      ? emptySet()
      : new HashSet<>(asList(scanScopedBeans.value()));

    ApplicationContext applicationContext = testContext.getApplicationContext();
    String[] mockBeanNames = applicationContext.getBeanDefinitionNames();
    List<Object> mockedBeans = new ArrayList<>();

    for (String beanName : mockBeanNames) {
      BeanDefinition beanDefinition = ((BeanDefinitionRegistry)applicationContext).getBeanDefinition(beanName);
      if (beanDefinition.isAbstract() || beanDefinition.isLazyInit()) {
        continue;
      }
      if (beanDefinition.isSingleton() || scanScopedBean(scanScopedBeans, scopes, beanDefinition)) {
        Object bean = applicationContext.getBean(beanName);
        if (mockUtil.isMock(bean)) {
          mockUtil.attachMock(bean, specification);
          mockedBeans.add(bean);
        }
      }
    }

    testContext.setAttribute(MOCKED_BEANS_LIST, mockedBeans);
  }

  private boolean scanScopedBean(ScanScopedBeans scanScopedBeans, Set<String> scopes, BeanDefinition beanDefinition) {
    return scanScopedBeans != null && (scopes.size() == 0 || scopes.contains(beanDefinition.getScope()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public void afterTestMethod(SpringTestContext testContext) throws Exception {
    List<Object> mockedBeans = (List<Object>) testContext.getAttribute(MOCKED_BEANS_LIST);

    if (mockedBeans != null) {
      for (Object object : mockedBeans) {
        mockUtil.detachMock(object);
      }
    }
  }

  @Override
  public void afterTestClass(SpringTestContext testContext) throws Exception {
  }
}
