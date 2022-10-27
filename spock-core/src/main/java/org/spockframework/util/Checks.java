package org.spockframework.util;

import org.jetbrains.annotations.Contract;
import org.junit.platform.commons.PreconditionViolationException;

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
   * @param object the object to check
   * @param messageProvider precondition violation message
   * @return the supplied object as a convenience
   * @throws PreconditionViolationException if the supplied object is {@code null}
   * @see #notNull(Object, Supplier)
   */
  public static <T> T notNull(T object, Supplier<String> messageProvider) throws PreconditionViolationException {
    checkArgument(object != null, messageProvider);
    return object;
  }

  /**
   * Assert that the supplied array is neither {@code null} nor <em>empty</em>.
   *
   * <p><strong>WARNING</strong>: this method does NOT check if the supplied
   * array contains any {@code null} elements.
   *
   * @param array the array to check
   * @param messageProvider precondition violation message
   * @return the supplied array as a convenience
   * @see #containsNoNullElements(Object[], Supplier)
   */
  public static <T> T[] notEmpty(T[] array, Supplier<String> messageProvider) {
    checkArgument(array != null && array.length > 0, messageProvider);
    return array;
  }

  /**
   * Assert that the supplied array contains no {@code null} elements.
   *
   * <p><strong>WARNING</strong>: this method does NOT check if the supplied
   * array is {@code null} or <em>empty</em>.
   *
   * @param array the array to check
   * @param messageProvider precondition violation message
   * @return the supplied array as a convenience
   * @see #notNull(Object, Supplier)
   */
  public static <T> T[] containsNoNullElements(T[] array, Supplier<String> messageProvider) throws PreconditionViolationException {
    if (array != null) {
      Arrays.stream(array).forEach(object -> notNull(object, messageProvider));
    }
    return array;
  }
}
