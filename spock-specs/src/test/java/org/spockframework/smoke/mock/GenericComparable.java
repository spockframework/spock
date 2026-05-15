package org.spockframework.smoke.mock;

import org.jetbrains.annotations.NotNull;

public class GenericComparable implements Comparable<GenericComparable>{
  private final int value;

  public GenericComparable(int value) {
    this.value = value;
  }

  @Override
  public int compareTo(@NotNull GenericComparable o) {
    return Integer.compare(value, o.value);
  }
}
