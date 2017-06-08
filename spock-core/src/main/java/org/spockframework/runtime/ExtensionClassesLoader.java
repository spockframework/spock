/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.runtime;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.spockframework.runtime.extension.ExtensionException;
import org.spockframework.runtime.extension.IGlobalExtension;
import org.spockframework.util.IoUtil;

/**
 * Scans class path for extension descriptors and loads the extension classes specified therein.
 */
public class ExtensionClassesLoader {
  public static final String EXTENSION_DESCRIPTOR_PATH = "META-INF/services/" + IGlobalExtension.class.getName();

  public List<Class<?>> loadClassesFromDefaultLocation() {
    return loadClasses(EXTENSION_DESCRIPTOR_PATH);
  }

  public List<Class<?>> loadClasses(String descriptorPath) {
    Set<Class<?>> extClasses = new HashSet<Class<?>>();
    for (URL url : locateDescriptors(descriptorPath))
      for (String className : readDescriptor(url))
        extClasses.add(loadExtensionClass(className));
    return new ArrayList<Class<?>>(extClasses);
  }

  private List<URL> locateDescriptors(String descriptorPath) {
    try {
      return Collections.list(RunContext.class.getClassLoader().getResources(descriptorPath));
    } catch (Exception e) {
      throw new ExtensionException("Failed to locate extension descriptors", e);
    }
  }

  private List<String> readDescriptor(URL url) {
    BufferedReader reader = null;

    try {
      reader = new BufferedReader(new InputStreamReader(url.openStream()));
      List<String> lines = new ArrayList<String>();
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
    } finally {
      IoUtil.closeQuietly(reader);
    }
  }

  private Class<?> loadExtensionClass(String className) {
    try {
      return RunContext.class.getClassLoader().loadClass(className);
    } catch (Exception e) {
      throw new ExtensionException("Failed to load extension class '%s'", e).withArgs(className);
    }
  }
}
