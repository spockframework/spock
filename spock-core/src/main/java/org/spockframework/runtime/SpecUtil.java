/*
 * Copyright 2009 the original author or authors.
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

import org.spockframework.runtime.model.*;
import spock.lang.Specification;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

import static java.util.Collections.sort;

/**
 * Utility methods related to specifications. Particularly useful when
 * integrating Spock with other environments (e.g. Grails).
 *
 * @author Peter Niederwieser
 */
public final class SpecUtil {
  private SpecUtil() {}

  /**
   * Tells if the given class is a Spock specification. Might return <tt>false</tt>
   * even though the class implements <tt>spock.lang.Specification</tt>. This can
   * happen if the class wasn't compiled properly (i.e. Spock's AST transform wasn't run).
   */
  public static boolean isSpec(Class<?> clazz) {
    return clazz.isAnnotationPresent(SpecMetadata.class);
  }

  /**
   * Checks if the given class is a Spock specification (according to <tt>isSpec()</tt>),
   * and throws an <tt>InvalidSpecException</tt> with a detailed explanation if it is not.
   */
  public static void checkIsSpec(Class<?> clazz) {
    if (isSpec(clazz)) return;

    if (Specification.class.isAssignableFrom(clazz))
      throw new InvalidSpecException(
"Specification '%s' was not compiled properly (Spock AST transform was not run); try to do a clean build"
      ).withArgs(clazz.getName());

    throw new InvalidSpecException(
"Class '%s' is not a Spock specification (does not extend spock.lang.Specification or a subclass thereof)"
    ).withArgs(clazz.getName());
  }

  public static boolean isRunnableSpec(Class<?> clazz) {
    return isSpec(clazz) && !Modifier.isAbstract(clazz.getModifiers());
  }

  public static void checkIsRunnableSpec(Class<?> clazz) {
    checkIsSpec(clazz);

    if (Modifier.isAbstract(clazz.getModifiers()))
      throw new InvalidSpecException("Specification '%s' is not runnable because it is declared abstract")
          .withArgs(clazz.getName());
  }

  /**
   * Returns the number of features contained in the given specification.
   * Because Spock allows for the dynamic creation of new features at
   * specification run time, this number is only an estimate.
   */
  public static int getFeatureCount(Class<?> spec) {
    checkIsSpec(spec);

    int count = 0;

    do {
      for (Method method : spec.getDeclaredMethods())
        if (method.isAnnotationPresent(FeatureMetadata.class))
          count++;
      spec = spec.getSuperclass();
    } while (spec != null && isSpec(spec));

    return count;
  }

  public static List<String> optimizeRunOrder(List<String> specNames) {
    List<SpecRunHistory> histories = loadHistories(specNames);
    sort(histories);
    return extractNames(histories);
  }

  public static <T> T getConfiguration(Class<T> type) {
    return RunContext.get().getConfiguration(type);
  }

  private static List<SpecRunHistory> loadHistories(List<String> specNames) {
    List<SpecRunHistory> histories = new ArrayList<>(specNames.size());

    for (String name : specNames) {
      SpecRunHistory history = new SpecRunHistory(name);
      try {
        history.loadFromDisk();
      } catch (IOException ignored) {} // history stays empty, so spec will be run early on
      histories.add(history);
    }

    return histories;
  }

  private static List<String> extractNames(List<SpecRunHistory> histories) {
    List<String> specNames = new ArrayList<>(histories.size());
    for (SpecRunHistory history : histories)
      specNames.add(history.getSpecName());
    return specNames;
  }
}
