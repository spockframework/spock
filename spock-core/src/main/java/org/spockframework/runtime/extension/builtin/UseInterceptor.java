/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.runtime.extension.builtin;

import java.util.List;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;

import groovy.lang.Closure;

/**
 * @author Peter Niederwieser
 */
public class UseInterceptor implements IMethodInterceptor {
  private final List<Class> categories;

  public UseInterceptor(List<Class> categories) {
    this.categories = categories;
  }

  public void intercept(final IMethodInvocation invocation) throws Throwable {
    DefaultGroovyMethods.use(null, categories,
        new Closure(invocation.getTarget(), invocation.getTarget()) {
          public Object doCall(Object[] args) throws Throwable {
            invocation.proceed();
            return null;
          }
        });
  }
}