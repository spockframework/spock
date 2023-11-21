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

package org.spockframework.runtime;

import org.spockframework.runtime.extension.*;
import org.spockframework.util.*;
import spock.config.ConfigurationObject;

import java.lang.reflect.*;
import java.util.*;

import org.jetbrains.annotations.NotNull;

/**
 * Maintains a registry of global Spock extensions and their configuration objects,
 * which can be used to configure other extensions.
 *
 * @author Peter Niederwieser
 */
public class GlobalExtensionRegistry implements IExtensionRegistry, IConfigurationRegistry {
  private final List<Class<? extends IGlobalExtension>> globalExtensionClasses;
  private final Map<Class<?>, Object> configurationsByType = new HashMap<>();
  private final Map<String, Object> configurationsByName = new HashMap<>();

  private final List<IGlobalExtension> globalExtensions = new ArrayList<>();

  GlobalExtensionRegistry(List<Class<? extends IGlobalExtension>> globalExtensionClasses,
                          List<Class<?>> initialConfigurations) {
    this.globalExtensionClasses = globalExtensionClasses;
    initializeConfigurations(initialConfigurations);
  }

  private void initializeConfigurations(List<Class<?>> initialConfigurations) {
    for (Class<?> configuration : initialConfigurations) {
      ConfigurationObject annotation = configuration.getAnnotation(ConfigurationObject.class);
      if (annotation == null) {
        throw new InternalSpockError("Not a @ConfigurationObject: %s").withArgs(configuration);
      }
      createAndStoreConfiguration(configuration, annotation);
    }
  }

  public void initializeGlobalExtensions() {
    for (Class<? extends IGlobalExtension> clazz : globalExtensionClasses) {
      verifyGlobalExtension(clazz);
      IGlobalExtension extension = instantiateAndConfigureExtension(clazz);
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

  private <T> T instantiateAndConfigureExtension(Class<T> clazz) {
    try {
      Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();
      Constructor<?> declaredConstructor = findBestFittingConstructor(declaredConstructors);
      if (declaredConstructor.getParameterCount() == 0) {
        Object extension = declaredConstructor.newInstance();
        configureExtension(extension);
        return clazz.cast(extension);
      } else {
        Object[] args = Arrays.stream(declaredConstructor.getParameters())
          .map(parameter -> getInjectionParameter(parameter, clazz))
          .toArray();

        return clazz.cast(declaredConstructor.newInstance(args));
      }
    } catch (Exception e) {
      throw new ExtensionException("Failed to instantiate extension '%s'", e).withArgs(clazz.getName());
    }

  }

  private Constructor<?> findBestFittingConstructor(Constructor<?>[] declaredConstructors) {
    return Arrays.stream(declaredConstructors)
      .filter(constructor -> Arrays.stream(constructor.getParameterTypes()).allMatch(this::isInjectable))
      .max(Comparator.comparing(Constructor::getParameterCount))
      .orElseThrow(() -> new ExtensionException("No suitable constructor found, only injectable parameters are permissible."));
  }

  private boolean isInjectable(Class<?> aClass) {
    ConfigurationObject annotation = aClass.getAnnotation(ConfigurationObject.class);
    return annotation != null;
  }

  @Override
  public <T> T instantiateExtension(Class<T> extension) {
    return instantiateAndConfigureExtension(extension);
  }

  private void configureExtension(Object extension) {
    Arrays.stream(extension.getClass().getDeclaredFields())
      .filter(field -> !Modifier.isFinal(field.getModifiers()))
      .forEach(field -> {
        ConfigurationObject annotation = field.getType().getAnnotation(ConfigurationObject.class);
        if (annotation != null) {
          injectConfiguration(field, annotation, extension);
        }
      });
  }

  public void startGlobalExtensions() {
    for (IGlobalExtension extension : globalExtensions) {
      extension.start();
    }
  }

  public void startExecutionForGlobalExtensions(ISpockExecution spockExecution) {
    for (IGlobalExtension extension : globalExtensions) {
      extension.executionStart(spockExecution);
    }
  }

  public void stopExecutionForGlobalExtensions(SpockExecution spockExecution) {
    for (IGlobalExtension extension : globalExtensions) {
      extension.executionStop(spockExecution);
    }
  }

  public void stopGlobalExtensions() {
    for (IGlobalExtension extension : globalExtensions) {
      extension.stop();
    }
  }

  private void injectConfiguration(Field field, ConfigurationObject annotation, Object extension) {
    Object config = getOrCreateConfig(field.getType(), annotation, extension.getClass());
    field.setAccessible(true);
    try {
      field.set(extension, config);
    } catch (IllegalAccessException e) {
      throw new UnreachableCodeError();
    }
  }

  @NotNull
  private Object getOrCreateConfig(Class<?> configType, ConfigurationObject annotation, Class<?> extensionClass) {
    Object config;
    if (IGlobalExtension.class.isAssignableFrom(extensionClass)) {
      config = getOrCreateConfiguration(configType, annotation);
    } else { // local extensions may not initialize configurations
      config = getConfigurationByType(configType);
      if (config == null) {
        throw new ExtensionException("Extension '%s' references unknown configuration class '%s'")
          .withArgs(extensionClass, configType);
      }
    }
    return config;
  }

  private Object getInjectionParameter(Parameter parameter, Class<?> extensionClass) {
    ConfigurationObject annotation = parameter.getType().getAnnotation(ConfigurationObject.class);
    return getOrCreateConfig(parameter.getType(), annotation, extensionClass);
  }

  private Object getOrCreateConfiguration(Class<?> type, ConfigurationObject annotation) {
    Object config = getConfigurationByType(type);
    if (config == null) {
      config = createAndStoreConfiguration(type, annotation);
    }
    return config;
  }

  private Object createAndStoreConfiguration(Class<?> configuration, ConfigurationObject annotation) {
    Object instance = createConfiguration(configuration);
    configurationsByType.put(configuration, instance);
    configurationsByName.put(annotation.value(), instance);
    return instance;
  }

  @NotNull
  private Object createConfiguration(Class<?> type) {
    try {
      return type.getDeclaredConstructor().newInstance();
    } catch (InstantiationException e) {
      throw new ExtensionException("Cannot instantiate configuration class %s", e).withArgs(type);
    } catch (IllegalAccessException | NoSuchMethodException e) {
      throw new ExtensionException("Configuration class '%s' has no public no-arg constructor", e).withArgs(type);
    } catch (Exception e) {
      throw new ExtensionException("Failed to instantiate configuration '%s'", e).withArgs(type.getName());
    }
  }
}
