/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.runtime;

import org.spockframework.runtime.extension.*;
import spock.config.ConfigurationObject;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Scans class path for extension descriptors and loads the extension classes specified therein.
 */
public class ExtensionClassesLoader {
  public static final String EXTENSION_DESCRIPTOR_PATH = "META-INF/services/" + IGlobalExtension.class.getName();
  public static final String CONFIG_DESCRIPTOR_PATH = "META-INF/services/" + ConfigurationObject.class.getName();

  public List<Class<? extends IGlobalExtension>> loadExtensionClassesFromDefaultLocation() {
    return loadClasses(EXTENSION_DESCRIPTOR_PATH, IGlobalExtension.class);
  }

  public List<Class<?>> loadConfigClassesFromDefaultLocation() {
    return loadClasses(CONFIG_DESCRIPTOR_PATH, Object.class);
  }

  public <T> List<Class<? extends T>> loadClasses(String descriptorPath, Class<T> baseClass) {
    Map<String, URL> discoveredClasses = new HashMap<>();
    List<Class<? extends T>> extClasses = new ArrayList<>();
    for (URL url : locateDescriptors(descriptorPath)) {
      for (String className : readDescriptor(url)) {
        if (discoveredClasses.containsKey(className)) {
          throw new ExtensionException("Duplicated Extension declaration for [%s]\nSource 1: %s\nSource 2: %s\n" +
            "This is most likely caused by having two different version of a library on the classpath."
          ).withArgs(className, url, discoveredClasses.get(className));
        }
        discoveredClasses.put(className, url);
        extClasses.add(loadExtensionClass(className, baseClass));
      }
    }
    return extClasses;
  }

  private List<URL> locateDescriptors(String descriptorPath) {
    try {
      return Collections.list(RunContext.class.getClassLoader().getResources(descriptorPath));
    } catch (Exception e) {
      throw new ExtensionException("Failed to locate extension descriptors", e);
    }
  }

  private List<String> readDescriptor(URL url) {
    try(BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
      List<String> lines = new ArrayList<>();
      String line = reader.readLine();
      while (line != null) {
        line = line.trim();
        if (line.length() > 0 && !line.startsWith("#"))
          lines.add(line);
        line = reader.readLine();
      }
      return lines;
    } catch (IOException e) {
      throw new ExtensionException("Failed to read extension descriptor '%s'", e).withArgs(url);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> Class<? extends T> loadExtensionClass(String className, Class<T> baseClass) {
    try {
      Class<?> loadedClass = RunContext.class.getClassLoader().loadClass(className);
      if (!baseClass.isAssignableFrom(loadedClass)) {
        throw new ExtensionException("Failed to load extension class '%s' as it is not assignable to '%s'")
          .withArgs(className, baseClass.getName());
      }
      return (Class<? extends T>)loadedClass;
    } catch (Exception e) {
      throw new ExtensionException("Failed to load extension class '%s'", e).withArgs(className);
    }
  }
}
