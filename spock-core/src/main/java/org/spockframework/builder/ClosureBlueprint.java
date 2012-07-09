/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.builder;

import groovy.lang.Closure;

import org.spockframework.runtime.GroovyRuntimeUtil;

public class ClosureBlueprint implements IBlueprint {
  private final Closure closure;
  private final Object subject;
  
  public ClosureBlueprint(Closure closure, Object subject) {
    this.closure = closure;
    this.subject = subject;
    closure.setResolveStrategy(Closure.DELEGATE_ONLY);
  }

  public Object getThisObject() {
    return closure.getThisObject();
  }

  public void setDelegate(Object delegate) {
    closure.setDelegate(delegate);
  }

  public void evaluate() {
    GroovyRuntimeUtil.invokeClosure(closure, subject);
  }
}