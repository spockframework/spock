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

package org.spockframework.spring;

import org.springframework.test.context.*;

@SuppressWarnings("EmptyMethod")
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
