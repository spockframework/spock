/*
 * Copyright 2015 the original author or authors.
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

package org.spockframework.mock.runtime;

import org.spockframework.mock.CannotCreateMockException;
import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.IResponseGenerator;
import org.spockframework.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DefaultMethodInvoker implements IResponseGenerator {
  private final Object target;
  private final Method method;
  private final Object[] arguments;

  public DefaultMethodInvoker(Object target, Method method, Object[] arguments) {
    this.target = target;
    this.method = method;
    this.arguments = arguments;
  }

  public Object respond(IMockInvocation invocation) {
    // This implementation uses classes from the java.lang.invoke package in order to invoke a default method.
    // Without exception handling, the implementation is analogous to the following code:
    //      final Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
    //      field.setAccessible(true);
    //      final MethodHandles.Lookup implLookup = (MethodHandles.Lookup) field.get(null);
    //      return implLookup.unreflectSpecial(method, method.getDeclaringClass()).bindTo(target).invokeWithArguments((Object)arguments);
    // The java.lang.invoke package is only available since Java 7. In order to preserve the compatibility of spock-core
    // with older versions of Java, we rewrite the above code using reflection.

    Object boundHandle = null;
    Method invokeWithArgumentsMethod = null;
    try {
      // Get the private field Lookup.IMPL_LOOKUP, which is maximally privileged
      Class<?> lookupClass = Class.forName("java.lang.invoke.MethodHandles$Lookup");
      final Field field = lookupClass.getDeclaredField("IMPL_LOOKUP");
      field.setAccessible(true);
      Object implLookup = field.get(null);

      // Get a method handle for the default method
      Method unreflectSpecialMethod = lookupClass.getMethod("unreflectSpecial", Method.class, Class.class);
      Object specialHandle = unreflectSpecialMethod.invoke(implLookup, method, method.getDeclaringClass());

      // Get a bound handle that prepends the target to the original arguments
      Method bindToMethod = specialHandle.getClass().getMethod("bindTo", Object.class);
      boundHandle = bindToMethod.invoke(specialHandle, target);

      // Get the method MethodHandle.invokeWithArguments(Object...)
      invokeWithArgumentsMethod = boundHandle.getClass().getMethod("invokeWithArguments", Object[].class);
    } catch (Exception e) {
      throw new CannotCreateMockException(target.getClass(), "Failed to invoke default method '" + method.getName() + "'", e);
    }

    // Call boundHandle.invokeWithArguments(arguments), sneaky throwing possible exceptions
    Object result = ReflectionUtil.invokeMethod(boundHandle, invokeWithArgumentsMethod, (Object)arguments);
    return result;
  }
}
