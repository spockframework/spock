package org.spockframework.util;

import java.lang.reflect.Method;

public class ThreadSupport {
  private static final @Nullable Class<?> THREAD_OF_VIRTUAL_CLASS = ReflectionUtil.loadClassIfAvailable("java.lang.Thread$Builder$OfVirtual");
  private static final @Nullable Method OF_VIRTUAL = THREAD_OF_VIRTUAL_CLASS != null ? ReflectionUtil.getMethodByName(Thread.class, "ofVirtual") : null;
  private static final @Nullable Method THREAD_OF_VIRTUAL_NAME = THREAD_OF_VIRTUAL_CLASS != null ? ReflectionUtil.getMethodBySignature(THREAD_OF_VIRTUAL_CLASS, "name", String.class) : null;
  private static final @Nullable Method THREAD_OF_VIRTUAL_UNSTARTED = THREAD_OF_VIRTUAL_CLASS != null ? ReflectionUtil.getMethodBySignature(THREAD_OF_VIRTUAL_CLASS, "unstarted", Runnable.class) : null;

  /**
   * Creates a virtual thread if supported by the current JVM.
   *
   * @param name the name of the thread
   * @param target the target to run on the thread
   * @return the created thread
   */
  public static Thread virtualThreadIfSupported(String name, Runnable target) {
    if (THREAD_OF_VIRTUAL_CLASS == null) {
      return new Thread(target, name);
    }
    Object builder = ReflectionUtil.invokeMethod(Thread.class, OF_VIRTUAL);
    ReflectionUtil.invokeMethod(builder, THREAD_OF_VIRTUAL_NAME, name);
    return (Thread) ReflectionUtil.invokeMethod(builder, THREAD_OF_VIRTUAL_UNSTARTED, target);
  }
}
