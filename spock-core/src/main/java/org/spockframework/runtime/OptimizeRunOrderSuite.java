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

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.*;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

@RunWith(OptimizeRunOrderSuite.class)
public class OptimizeRunOrderSuite extends Suite {
  public static final String CLASSES_TO_RUN_KEY = OptimizeRunOrderSuite.class.getName() + ".classesToRun";

  public OptimizeRunOrderSuite(Class<?> clazz, RunnerBuilder builder) throws InitializationError {
    super(builder, clazz, loadAndReorderClassesToRun());
  }

  private static Class<?>[] loadAndReorderClassesToRun() throws InitializationError {
    List<String> classNames = getClassesToRun();
    SpecUtil.optimizeRunOrder(classNames);
    List<Class<?>> classes = loadClasses(classNames);
    return classes.toArray(new Class[classes.size()]);
  }

  private static List<String> getClassesToRun() throws InitializationError {
    String property = System.getProperty(CLASSES_TO_RUN_KEY);
    if (property == null)
      throw new InitializationError(String.format("system property '%s' is not set", CLASSES_TO_RUN_KEY));

    List<String> classNames = new ArrayList<String>();
    for (String name : property.split(","))
      classNames.add(name.trim());

    return classNames;
  }

  private static List<Class<?>> loadClasses(List<String> classNames) throws InitializationError {
    List<Class<?>> classes = new ArrayList<Class<?>>();
    ClassLoader classLoader = OptimizeRunOrderSuite.class.getClassLoader();

    try {
      for (String name : classNames)
        classes.add(classLoader.loadClass(name));
    } catch (ClassNotFoundException e) {
      throw new InitializationError(e);
    }

    return classes;
  }
}
