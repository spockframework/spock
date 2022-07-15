/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock.runtime;

import org.spockframework.mock.*;

import groovy.lang.MetaClass;

public class GroovyRealMethodInvoker implements IResponseGenerator {
  private final MetaClass metaClass;

  public GroovyRealMethodInvoker(MetaClass metaClass) {
    this.metaClass = metaClass;
  }

  @Override
  public Object respond(IMockInvocation invocation) {
    Object instance = invocation.getMockObject().getInstance();
    Object[] arguments = invocation.getArguments().toArray();
    if (invocation.getMethod().isStatic()) {
      if ("<init>".equals(invocation.getMethod().getName())) {
        return metaClass.invokeConstructor(arguments);
      }
      return metaClass.invokeStaticMethod(instance, invocation.getMethod().getName(), arguments);
    }
    return metaClass.invokeMethod(instance, invocation.getMethod().getName(), arguments);
  }
}
