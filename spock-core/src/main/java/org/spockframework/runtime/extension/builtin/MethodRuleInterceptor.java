/*
 * Copyright 2011 the original author or authors.
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

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;

import java.util.List;

public class MethodRuleInterceptor implements IMethodInterceptor {
  private final List<FieldInfo> ruleFields;

  MethodRuleInterceptor(List<FieldInfo> ruleFields) {
    this.ruleFields = ruleFields;
  }

  public void intercept(final IMethodInvocation invocation) throws Throwable {
    Statement statement = createBaseStatement(invocation);
    FrameworkMethod method = createFrameworkMethod(invocation);

    for (FieldInfo field : ruleFields) {
      Object rule = field.readValue(invocation.getTarget());
      if (!(rule instanceof org.junit.rules.MethodRule)) continue;

      statement = ((org.junit.rules.MethodRule) rule).apply(statement, method, invocation.getTarget());
    }

    statement.evaluate();
  }

  private Statement createBaseStatement(final IMethodInvocation invocation) {
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

