/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.mock.response;

import org.spockframework.mock.IMockInvocation;
import org.spockframework.runtime.GroovyRuntimeUtil;

import groovy.lang.Closure;

import static org.spockframework.util.ObjectUtil.uncheckedCast;

/**
 *
 * @author Peter Niederwieser
 */
public class CodeResponseGenerator extends SingleResponseGenerator {
  private final Closure code;

  public CodeResponseGenerator(Closure code) {
    this.code = code;
  }

  @Override
  public Object doRespond(IMockInvocation invocation) {
    Object result = invokeClosure(invocation);
    Class<?> returnType = invocation.getMethod().getReturnType();

    // don't attempt cast for void methods (closure could be an action that accidentally returns a value)
    if (returnType == void.class || returnType == Void.class) return null;

    return GroovyRuntimeUtil.coerce(result, invocation.getMethod().getReturnType());
  }

  private Object invokeClosure(IMockInvocation invocation) {
    Class<?>[] paramTypes = code.getParameterTypes();
    if (paramTypes.length == 1 && paramTypes[0] == IMockInvocation.class) {
      return GroovyRuntimeUtil.invokeClosure(code, invocation);
    }

    Closure<?> code = uncheckedCast(this.code.clone());
    code.setDelegate(invocation);
    code.setResolveStrategy(Closure.DELEGATE_FIRST);
    return GroovyRuntimeUtil.invokeClosure(code, invocation.getArguments());
  }
}
