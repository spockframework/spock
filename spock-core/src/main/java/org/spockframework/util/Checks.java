package org.spockframework.util;

import org.jetbrains.annotations.Contract;

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
}
