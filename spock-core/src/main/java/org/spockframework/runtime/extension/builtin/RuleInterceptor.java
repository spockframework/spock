/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin;

import java.util.List;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.FieldInfo;

public class RuleInterceptor implements IMethodInterceptor {
  private List<FieldInfo> ruleFields;

  public RuleInterceptor(List<FieldInfo> ruleFields) {
    this.ruleFields = ruleFields;
  }

  public void intercept(final IMethodInvocation invocation) throws Throwable {
    Statement stat = createStatement(invocation);
    FrameworkMethod method = createFrameworkMethod(invocation);

    for (FieldInfo field : ruleFields) {
      MethodRule rule = (MethodRule) field.readValue(invocation.getTarget());
      stat = rule.apply(stat, method, invocation.getTarget());
    }

    stat.evaluate();
  }

  private Statement createStatement(final IMethodInvocation invocation) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        invocation.proceed();
      }
    };
  }

  private FrameworkMethod createFrameworkMethod(final IMethodInvocation invocation) {
    return new FrameworkMethod(invocation.getMethod().getReflection()) {
      @Override
      public String getName() {
        return invocation.getMethod().getName();
      }
    };
  }

}
