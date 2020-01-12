/*
 * Copyright 2012 the original author or authors.
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

package org.spockframework.junit4;

import org.junit.runners.model.Statement;

import org.spockframework.runtime.extension.ExtensionException;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;

import java.util.List;

public abstract class AbstractRuleInterceptor implements IMethodInterceptor {
  protected final List<FieldInfo> ruleFields;

  public AbstractRuleInterceptor(List<FieldInfo> ruleFields) {
    this.ruleFields = ruleFields;
  }

  protected Statement createBaseStatement(final IMethodInvocation invocation) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        invocation.proceed();
      }
    };
  }

  protected Object getRuleInstance(FieldInfo field, Object fieldTarget) {
    Object rule = field.readValue(fieldTarget);
    if (rule == null) {
      try {
        rule = field.getType().newInstance();
        field.writeValue(fieldTarget, rule);
      } catch (Exception e) {
        throw new ExtensionException("Auto-instantiating @Rule field '%s' failed. You may have to instantiate it manually.", e).withArgs(field.getName());
      }
    }
    return rule;
  }
}
