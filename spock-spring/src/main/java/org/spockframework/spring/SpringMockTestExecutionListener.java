package org.spockframework.spring;

import org.spockframework.mock.MockUtil;
import org.spockframework.util.ReflectionUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestExecutionListener;
import spock.lang.Specification;

import java.util.*;

/**
 * This {@link TestExecutionListener} takes care of attaching and detaching the
 * mocks to the current {@link Specification}.
 *
 * @author Leonard Bruenings
 */
public class SpringMockTestExecutionListener extends AbstractSpringTestExecutionListener {

  public static final String MOCKED_BEANS_LIST = "org.spockframework.spring.SpringMockTestExecutionListener.MOCKED_BEANS_LIST";

  private final MockUtil mockUtil = new MockUtil();

  @Override
  public void beforeTestClass(SpringTestContext testContext) throws Exception {
  }

  @Override
  public void prepareTestInstance(SpringTestContext testContext) throws Exception {
  }

  @Override
  public void beforeTestMethod(SpringTestContext testContext) throws Exception {
    Object testInstance = testContext.getTestInstance();

    if (testInstance instanceof Specification) {
      Specification specification = (Specification) testInstance;
      ScanScopedBeans scanScopedBeans = ReflectionUtil.getAnnotationRecursive(specification.getClass(),
        ScanScopedBeans.class);
      Set<String> scopes = scanScopedBeans == null ? Collections.<String>emptySet() :
        new HashSet<String>(Arrays.asList(scanScopedBeans.value()));

      ApplicationContext applicationContext = testContext.getApplicationContext();
      String[] mockBeanNames = applicationContext.getBeanDefinitionNames();
      List<Object> mockedBeans = new ArrayList<Object>();

      for (String beanName : mockBeanNames) {
        BeanDefinition beanDefinition = ((BeanDefinitionRegistry)applicationContext).getBeanDefinition(beanName);
        if(beanDefinition.isAbstract()){
            continue;
        }
        if (beanDefinition.isSingleton() || scanScopedBean(scanScopedBeans, scopes, beanDefinition)){
          Object bean = applicationContext.getBean(beanName);
          if (mockUtil.isMock(bean)) {
            mockUtil.attachMock(bean, specification);
            mockedBeans.add(bean);
          }
        }
      }

      testContext.setAttribute(MOCKED_BEANS_LIST, mockedBeans);

    } else {
      throw new IllegalArgumentException("SpringMockTestExecutionListener is only applicable for spock specifications.");
    }
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
