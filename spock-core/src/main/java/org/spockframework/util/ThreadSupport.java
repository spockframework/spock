/*
 *  Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
