package org.spockframework.spring;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public abstract class AbstractSpringTestExecutionListener implements TestExecutionListener {
  @Override
  public void beforeTestClass(TestContext testContext) throws Exception {
    beforeTestClass(new SpringTestContext(testContext));
  }

  public abstract void beforeTestClass(SpringTestContext testContext) throws Exception;

  @Override
  public void prepareTestInstance(TestContext testContext) throws Exception {
    prepareTestInstance(new SpringTestContext(testContext));
  }

  public abstract void prepareTestInstance(SpringTestContext testContext) throws Exception;

  @Override
  public void beforeTestMethod(TestContext testContext) throws Exception {
    beforeTestMethod(new SpringTestContext(testContext));
  }

  public abstract void beforeTestMethod(SpringTestContext testContext) throws Exception;

  @Override
  public void afterTestMethod(TestContext testContext) throws Exception {
    afterTestMethod(new SpringTestContext(testContext));
  }

  public abstract void afterTestMethod(SpringTestContext testContext) throws Exception;

  @Override
  public void afterTestClass(TestContext testContext) throws Exception {
    afterTestClass(new SpringTestContext(testContext));
  }

  public abstract void afterTestClass(SpringTestContext testContext) throws Exception;
}
