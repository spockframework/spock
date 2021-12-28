/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.constraint;

import org.spockframework.mock.*;
import org.spockframework.runtime.Condition;
import org.spockframework.util.CollectionUtil;

import java.util.regex.Pattern;

/**
 *
 * @author Peter Niederwieser
 */
public class RegexMethodNameConstraint implements IInvocationConstraint {
  private final Pattern pattern;

  public RegexMethodNameConstraint(String regex) {
    pattern = Pattern.compile(regex);
  }

  @Override
  public boolean isSatisfiedBy(IMockInvocation invocation) {
    return pattern.matcher(invocation.getMethod().getName()).matches();
  }

  @Override
  public String describeMismatch(IMockInvocation invocation) {
    Condition condition = new Condition(CollectionUtil.listOf(invocation.getMethod().getName(), pattern.pattern(), false),
      String.format("methodName ==~ /%s/", pattern.pattern()), null, null,
      null, null);
    return condition.getRendering();
  }
}
