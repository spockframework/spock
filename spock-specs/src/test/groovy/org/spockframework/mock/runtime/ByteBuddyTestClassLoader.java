/*
 * Copyright 2023 the original author or authors.
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

package org.spockframework.mock.runtime;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import java.util.HashMap;
import java.util.Map;

public final class ByteBuddyTestClassLoader extends ClassLoader {
  private final Map<String, Class<?>> cache = new HashMap<>();

  private final ClassLoadingStrategy<ByteBuddyTestClassLoader> loadingStrategy = (loader, types) -> {
    Map<TypeDescription, Class<?>> result = new HashMap<>();
    for (Map.Entry<TypeDescription, byte[]> entry : types.entrySet()) {
      TypeDescription description = entry.getKey();
      byte[] bytes = entry.getValue();
      Class<?> clazz = defineClass(description.getName(), bytes, 0, bytes.length);
      result.put(description, clazz);
    }
    return result;
  };

  /**
   * Creates a new {@link ByteBuddyTestClassLoader} and defines an interface with the passed name.
   *
   * @param name the interface name
   * @return the classloader
   */
  public static ByteBuddyTestClassLoader withInterface(String name) {
    ByteBuddyTestClassLoader cl = new ByteBuddyTestClassLoader();
    cl.defineInterface(name);
    return cl;
  }

  /**
   * Defines an empty interface with the passed {@code node}.
   *
   * @param name the name of the interface
   * @return the loaded {@code Class}
   */
  public synchronized Class<?> defineInterface(String name) {
    //noinspection resource
    return cache.computeIfAbsent(name, nameKey -> new ByteBuddy()
      .makeInterface()
      .name(nameKey)
      .make()
      .load(this, loadingStrategy)
      .getLoaded());
  }
}
