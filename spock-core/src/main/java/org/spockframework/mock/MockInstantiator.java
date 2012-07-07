package org.spockframework.mock;

import org.objenesis.ObjenesisHelper;
import org.spockframework.util.ReflectionUtil;

import java.lang.reflect.Constructor;

public class MockInstantiator {
  private static final boolean objenesisAvailable = ReflectionUtil.isClassAvailable("org.objenesis.Objenesis");

  public static Object instantiate(Class<?> declaredType, Class<?> actualType) {
    try {
      if (objenesisAvailable) {
        return ObjenesisInstantiator.instantiate(actualType);
      }

      Constructor<?> ctor = actualType.getDeclaredConstructor();
      boolean accessible = ctor.isAccessible();
      try {
        ctor.setAccessible(true);
        return ctor.newInstance();
      } finally {
        ctor.setAccessible(accessible);
      }
    } catch (Exception e) {
      String msg = objenesisAvailable ? null : ". Putting Objenesis (1.2 or higher) on the class path will likely solve this problem.";
      throw new CannotCreateMockException(declaredType, msg, e);
    }
  }

  // inner class to defer class loading
  private static class ObjenesisInstantiator {
    static Object instantiate(Class<?> clazz) {
      return clazz.cast(ObjenesisHelper.newInstance(clazz));
    }
  }
}
