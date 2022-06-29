/*
 * Copyright 2015 the original author or authors.
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

import org.spockframework.mock.CannotCreateMockException;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.IResponseGenerator;
import org.spockframework.util.ExceptionUtil;
import org.spockframework.util.Nullable;
import org.spockframework.util.ReflectionUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DefaultMethodInvoker implements IResponseGenerator {
  @Nullable // Available since Java 17
  private static final Method INVOKE_DEFAULT = ReflectionUtil.getDeclaredMethodByName(InvocationHandler.class, "invokeDefault");
  private final Object target;
  private final Method method;
  private final Object[] arguments;

  public DefaultMethodInvoker(Object target, Method method, Object[] arguments) {
    this.target = target;
    this.method = method;
    this.arguments = arguments;
  }

  @Override
  public Object respond(IMockInvocation invocation) {
    if (INVOKE_DEFAULT == null) {
      return useInternalMethodHandle();
    }
    Object[] args = new Object[]{target, method, arguments};
    return ReflectionUtil.invokeMethod(null, INVOKE_DEFAULT, args);
  }

  private Object useInternalMethodHandle() {
    MethodHandle methodHandle;
    try {
      final Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
      field.setAccessible(true);
      final MethodHandles.Lookup implLookup = (MethodHandles.Lookup) field.get(null);
      methodHandle = implLookup.unreflectSpecial(method, method.getDeclaringClass()).bindTo(target);
    } catch (Exception e) {
      throw new CannotCreateMockException(target.getClass(), "Failed to invoke default method '" + method.getName() + "'. Adding '--add-opens=java.base/java.lang.invoke=ALL-UNNAMED' might fix this.", e);
    }

    try {
      return methodHandle.invokeWithArguments(arguments);
    } catch (Throwable e) {
      return ExceptionUtil.sneakyThrow(e);
    }
  }
}
