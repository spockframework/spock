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

package org.spockframework.runtime;

import java.lang.reflect.Field;
import java.util.*;

import org.spockframework.runtime.extension.ExtensionException;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.util.UnreachableCodeError;

/**
 * Collects all Spock extensions found on the class path (via their descriptor).
 *
 * @author Peter Niederwieser
 */
public class ExtensionRegistry {
  private final List<Class<?>> extensionClasses;
  private final List<Object> configurations;

  private final List<IGlobalExtension> extensions = new ArrayList<IGlobalExtension>();

  ExtensionRegistry(List<Class<?>> extensionClasses, List<Object> configurations) {
    this.extensionClasses = extensionClasses;
    this.configurations = configurations;
  }

  public void loadExtensions() {
    for (Class<?> clazz : extensionClasses)
      extensions.add(configureExtension(instantiateExtension(verifyExtensionClass(clazz))));
  }

  public List<IGlobalExtension> getExtensions() {
    return extensions;
  }

  public List<Object> getConfigurations() {
    return configurations;
  }

  private Class<?> verifyExtensionClass(Class<?> clazz) {
    if (!IGlobalExtension.class.isAssignableFrom(clazz))
      throw new ExtensionException(
          "Class '%s' is not a valid global extension because it is not derived from '%s'"
      ).format(clazz.getName(), IGlobalExtension.class.getName());
    return clazz;
  }

  private IGlobalExtension instantiateExtension(Class<?> clazz) {
    try {
      return (IGlobalExtension)clazz.newInstance();
    } catch (Exception e) {
      throw new ExtensionException("Failed to instantiate extension '%s'", e).format(clazz.getName());
    }
  }

  private IGlobalExtension configureExtension(IGlobalExtension extension) {
    for (Field field : extension.getClass().getDeclaredFields())
      if (field.getType().getSimpleName().endsWith("Configuration"))
        injectConfiguration(field, extension);

    return extension;
  }

  private void injectConfiguration(Field field, Object extension) {
    Object config = getOrCreateConfiguration(field.getType());
    field.setAccessible(true);
    try {
      field.set(extension, config);
    } catch (IllegalAccessException e) {
      throw new UnreachableCodeError();
    }
  }

  private Object getOrCreateConfiguration(Class<?> type) {
    for (Object config : configurations)
      if (config.getClass() == type) return config;

    Object config = createConfiguration(type);
    configurations.add(config);
    return config;
  }

  private Object createConfiguration(Class<?> type) {
    try {
      return type.newInstance();
    } catch (InstantiationException e) {
      // TODO: need better exception type
      throw new RuntimeException(String.format("Cannot instantiate configuration class %s", type));
    } catch (IllegalAccessException e) {
      throw new RuntimeException(String.format("Configuration class '%s' has no public no-arg constructor", type));
    }
  }
}
