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

package org.spockframework.runtime.intercept;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.util.InternalSpockError;

/**
 *
 * @author Peter Niederwieser
 */
public class MethodInvocation implements IMethodInvocation {
  private final Object target;
  private final MethodInfo method;
  private final Object[] arguments;
  private final Iterator<IMethodInterceptor> interceptors;

  public MethodInvocation(Object target, MethodInfo method, Object[] arguments) {
    this.target = target;
    this.method = method;
    this.arguments = arguments;
    interceptors = method.getInterceptors().iterator();
  }

  public Object getTarget() {
    return target;
  }

  public MethodInfo getMethod() {
    return method;
  }

  public Object[] getArguments() {
    return arguments;
  }

  public void proceed() throws Throwable {
    if (interceptors.hasNext())
      interceptors.next().invoke(this);
    else invokeTargetMethod();
  }

  protected void invokeTargetMethod() throws Throwable {
    if (method.isStub()) return;

    try {
      method.getReflection().invoke(target, arguments);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    } catch (Throwable t) {
      throw new InternalSpockError("Failed to invoke method '%s'", t).withArgs(method.getReflection().getName());
    }
  }
}
