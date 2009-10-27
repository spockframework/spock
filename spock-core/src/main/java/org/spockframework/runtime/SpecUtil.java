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

import java.lang.reflect.Method;

import org.spockframework.runtime.model.FeatureMetadata;
import org.spockframework.runtime.model.SpeckMetadata;

/**
 * Utility methods related to specifications. Particularly useful when
 * integrating Spock with other environments (e.g. Grails).
 *
 * @author Peter Niederwieser
 */
public final class SpecUtil {
  private SpecUtil() {}

  /**
   * Tells if the given class is a Spock specification.
   */
  public static boolean isSpec(Class<?> clazz) {
    return clazz.isAnnotationPresent(SpeckMetadata.class);
  }

  /**
   * Checks if the given class is a Spock specification, and throws an
   * <tt>IllegalArgumentException</tt> if it is not.
   */
  public static void checkIsSpec(Class<?> clazz) {
    if (!isSpec(clazz))
      throw new IllegalArgumentException(
          String.format("class %s is not a Spock specification", clazz.getSimpleName()));
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
}
