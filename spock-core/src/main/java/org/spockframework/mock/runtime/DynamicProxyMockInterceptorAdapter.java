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

package org.spockframework.mock.runtime;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class DynamicProxyMockInterceptorAdapter implements InvocationHandler {
  private final IProxyBasedMockInterceptor interceptor;

  public DynamicProxyMockInterceptorAdapter(IProxyBasedMockInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  public Object invoke(Object target, Method method, Object[] arguments) throws Throwable {
    if(isDefault(method)) {
      // The commented out code below uses classes from the java.lang.invoke package, which is only available since Java 7.
      // In order to preserve the compatibility of spock-core with older versions of Java, we rewrite this code using reflection.
      // Since the current if-block is executed only when handling a default method, and since default methods have benn
      // introduced in Java 8, we can safely assume that the java.lang.invoke classes are available at run time.
/*
      final Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
      field.setAccessible(true);
      final MethodHandles.Lookup lookup = (MethodHandles.Lookup) field.get(null);
      final Object result = lookup
        .unreflectSpecial(method, method.getDeclaringClass())
        .bindTo(target)
        .invokeWithArguments();
*/
      Class<?> lookupClass = Class.forName("java.lang.invoke.MethodHandles$Lookup");
      final Field field = lookupClass.getDeclaredField("IMPL_LOOKUP");
      field.setAccessible(true);
      Object implLookup = field.get(null);
      Method unreflectSpecialMethod = lookupClass.getMethod("unreflectSpecial", Method.class, Class.class);
      Object specialHandle = unreflectSpecialMethod.invoke(implLookup, method, method.getDeclaringClass());
      Method bindToMethod = specialHandle.getClass().getMethod("bindTo", Object.class);
      Object bindHandle = bindToMethod.invoke(specialHandle, target);
      Method invokeWithArgumentsMethod = bindHandle.getClass().getMethod("invokeWithArguments", Object[].class);
      Object result = invokeWithArgumentsMethod.invoke(bindHandle, (Object)new Object[0]);
      return result;
    }
    return interceptor.intercept(target, method, arguments,
        new FailingRealMethodInvoker("Cannot invoke real method on interface based mock object"));
  }

  /**
   * Returns {@code true} if the argument {@code m} is a default method; returns {@code false} otherwise.
   * <br/>This method is used instead of {@link Method#isDefault()} in order to preserve the compatibility with Java versions prior to java 8.
   *
   * @param m the method to be checked whether it is default or not
   * @return true if and only if the argument {@code m} is a default method as defined by the Java Language Specification.
   */
  public static boolean isDefault(Method m) {
    // Default methods are public non-abstract instance methods declared in an interface.
    return ((m.getModifiers() & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) ==
      Modifier.PUBLIC) && m.getDeclaringClass().isInterface();
  }

}
