/*
 * Copyright 2023 the original author or authors.
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
 *
 */

package org.spockframework.util;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Checks for use within Spock code. Failures indicate usage errors.
 *
 * @author Leonard Brünings
 * @since 2.4
 */
public abstract class Checks {

  @Contract("false, _ -> fail")
  public static void checkState(boolean expression, Supplier<String> messageProvider) {
    if (!expression) {
      throw new IllegalStateException(messageProvider.get());
    }
  }

  @Contract("false, _ -> fail")
  public static void checkArgument(boolean expression, Supplier<String> messageProvider) {
    if (!expression) {
      throw new IllegalArgumentException(messageProvider.get());
    }
  }

  /**
   * Assert that the supplied {@link Object} is not {@code null}.
   *
   * @param object          the object to check
   * @param messageProvider violation message
   * @return the supplied object as a convenience
   * @throws IllegalArgumentException if the supplied object is {@code null}
   */
  public static <T> T notNull(T object, Supplier<String> messageProvider) {
    checkArgument(object != null, messageProvider);
    return object;
  }

  /**
   * Assert that the supplied array is neither {@code null} nor <em>empty</em>.
   * <p>
   * This method does NOT check if the supplied
   * array contains any {@code null} elements.
   *
   * @param array           the array to check
   * @param messageProvider violation message
   * @return the supplied array as a convenience
   * @throws IllegalArgumentException if the supplied array is {@code null} or contains no elements.
   * @see #containsNoNullElements(Object[], Supplier)
   */
  public static <T> T[] notEmpty(T[] array, Supplier<String> messageProvider) {
    checkArgument(array != null && array.length > 0, messageProvider);
    return array;
  }

  /**
   * Assert that the supplied array contains no {@code null} elements.
   * <p>
   * This method does NOT check if the supplied
   * array is {@code null} or <em>empty</em>.
   *
   * @param array           the array to check
   * @param messageProvider violation message
   * @return the supplied array as a convenience
   * @see #notNull(Object, Supplier)
   */
  public static <T> T[] containsNoNullElements(T[] array, Supplier<String> messageProvider) {
    if (array != null) {
      Arrays.stream(array).forEach(object -> notNull(object, messageProvider));
    }
    return array;
  }
}
