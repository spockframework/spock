package org.spockframework.spring;

import java.util.ArrayList;
import java.util.List;

import org.spockframework.mock.ISpockMockObject;
import org.spockframework.mock.MockUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

import spock.lang.Specification;

/**
 * This {@link TestExecutionListener} takes care of attaching and detaching the
 * mocks to the current {@link Specification}.
 *
 * @author Leonard Bruenings
 */
public class SpringMockTestExecutionListener implements TestExecutionListener {

  public static final String MOCKED_BEANS_LIST = "org.spockframework.spring.SpringMockTestExecutionListener.MOCKED_BEANS_LIST";

  private final MockUtil mockUtil = new MockUtil();

  public void beforeTestClass(TestContext testContext) throws Exception {
  }

  public void prepareTestInstance(TestContext testContext) throws Exception {
  }

  public void beforeTestMethod(TestContext testContext) throws Exception {
    Object testInstance = testContext.getTestInstance();

    if (testInstance instanceof Specification) {
      Specification specification = (Specification) testInstance;

      ApplicationContext applicationContext = testContext.getApplicationContext();
      String[] mockBeanNames = applicationContext.getBeanDefinitionNames();
      List<Object> mockedBeans = new ArrayList<Object>();

      for (String beanName : mockBeanNames) {
        BeanDefinition beanDefinition = ((BeanDefinitionRegistry)applicationContext).getBeanDefinition(beanName);
        if(beanDefinition.isAbstract()){
            continue;
        }
        Object bean = applicationContext.getBean(beanName);
        if (mockUtil.isMock(bean)) {
          mockUtil.attachMock(bean, specification);
          mockedBeans.add(bean);
        }
      }

      testContext.setAttribute(MOCKED_BEANS_LIST, mockedBeans);

    } else {
      throw new IllegalArgumentException("SpringMockTestExecutionListener is only applicable for spock specifications.");
    }
  }

  @SuppressWarnings("unchecked")
  public void afterTestMethod(TestContext testContext) throws Exception {
    List<Object> mockedBeans = (List<Object>) testContext.getAttribute(MOCKED_BEANS_LIST);

    if (mockedBeans != null) {
      for (Object object : mockedBeans) {
        mockUtil.detachMock(object);
      }
    }
  }

  public void afterTestClass(TestContext testContext) throws Exception {
  }

}
