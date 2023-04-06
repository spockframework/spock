/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

/**
 * @author Peter Niederwieser
 */
public class UseInterceptor implements IMethodInterceptor {
  private final List<Class> categories;

  public UseInterceptor(Set<Class> categories) {
    this.categories = new ArrayList<>(categories);
  }

  @Override
  public void intercept(final IMethodInvocation invocation) throws Throwable {
    DefaultGroovyMethods.use(null, categories,
        new Closure<Void>(invocation.getInstance(), invocation.getInstance()) {
          public Void doCall(Object[] args) throws Throwable {
            invocation.proceed();
            return null;
          }
        });
  }
}
