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

import org.spockframework.runtime.extension.*;
import org.spockframework.util.*;
import spock.config.ConfigurationObject;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Maintains a registry of global Spock extensions and their configuration objects,
 * which can be used to configure other extensions.
 *
 * @author Peter Niederwieser
 */
public class GlobalExtensionRegistry implements IExtensionRegistry, IConfigurationRegistry {
  private final List<Class<?>> globalExtensionClasses;
  private final Map<Class<?>, Object> configurationsByType = new HashMap<>();
  private final Map<String, Object> configurationsByName = new HashMap<>();

  private final List<IGlobalExtension> globalExtensions = new ArrayList<>();

  GlobalExtensionRegistry(List<Class<?>> globalExtensionClasses, List<?> initialConfigurations) {
    this.globalExtensionClasses = globalExtensionClasses;
    initializeConfigurations(initialConfigurations);
  }

  private void initializeConfigurations(List<?> initialConfigurations) {
    for (Object configuration : initialConfigurations) {
      ConfigurationObject annotation = configuration.getClass().getAnnotation(ConfigurationObject.class);
      if (annotation == null) {
        throw new InternalSpockError("Not a @ConfigurationObject: %s").withArgs(configuration.getClass());
      }
      configurationsByType.put(configuration.getClass(), configuration);
      configurationsByName.put(annotation.value(), configuration);
    }
  }

  public void initializeGlobalExtensions() {
    for (Class<?> clazz : globalExtensionClasses) {
      verifyGlobalExtension(clazz);
      IGlobalExtension extension = instantiateGlobalExtension(clazz);
      configureExtension(extension);
      globalExtensions.add(extension);
    }
  }

  @Override
  public <T> T getConfigurationByType(Class<T> clazz) {
    return clazz.cast(configurationsByType.get(clazz));
  }

  @Override
  public Object getConfigurationByName(String name) {
    return configurationsByName.get(name);
  }

  @Override
  public List<IGlobalExtension> getGlobalExtensions() {
    return globalExtensions;
  }

  private void verifyGlobalExtension(Class<?> clazz) {
    if (!IGlobalExtension.class.isAssignableFrom(clazz))
      throw new ExtensionException(
          "Class '%s' is not a valid global extension because it is not derived from '%s'"
      ).withArgs(clazz.getName(), IGlobalExtension.class.getName());
  }

  private IGlobalExtension instantiateGlobalExtension(Class<?> clazz) {
    try {
      return (IGlobalExtension) clazz.newInstance();
    } catch (Exception e) {
      throw new ExtensionException("Failed to instantiate extension '%s'", e).withArgs(clazz.getName());
    }
  }

  @Override
  public void configureExtension(Object extension) {
    for (Field field : extension.getClass().getDeclaredFields()) {
      ConfigurationObject annotation = field.getType().getAnnotation(ConfigurationObject.class);
      if (annotation != null) {
        injectConfiguration(field, annotation.value(), extension);
      }
    }
  }

  public void startGlobalExtensions() {
    for (IGlobalExtension extension : globalExtensions) {
      extension.start();
    }
  }

  public void stopGlobalExtensions() {
    for (IGlobalExtension extension : globalExtensions) {
      extension.stop();
    }
  }

  private void injectConfiguration(Field field, String name, Object extension) {
    Object config = getOrCreateConfiguration(field.getType(), name, extension);
    field.setAccessible(true);
    try {
      field.set(extension, config);
    } catch (IllegalAccessException e) {
      throw new UnreachableCodeError();
    }
  }

  private Object getOrCreateConfiguration(Class<?> type, String name, Object extension) {
    Object config = configurationsByType.get(type);
    if (config == null) {
      config = createConfiguration(type, extension);
      configurationsByType.put(type, config);
      configurationsByName.put(name, config);
    }
    return config;
  }

  private Object createConfiguration(Class<?> type, Object extension) {
    if (!(extension instanceof IGlobalExtension)) {
      throw new ExtensionException("Extension '%s' references unknown configuration class '%s'")
          .withArgs(extension.getClass(), type);
    }

    try {
      return type.newInstance();
    } catch (InstantiationException e) {
      throw new ExtensionException("Cannot instantiate configuration class %s").withArgs(type);
    } catch (IllegalAccessException e) {
      throw new ExtensionException("Configuration class '%s' has no public no-arg constructor").withArgs(type);
    }
  }
}
