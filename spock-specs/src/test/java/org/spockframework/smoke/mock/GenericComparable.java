package org.spockframework.smoke.mock;

public class GenericComparable implements Comparable<GenericComparable>{
  private final int value;

  public GenericComparable(int value) {
    this.value = value;
  }

  @Override
  public int compareTo(GenericComparable o) {
    return Integer.compare(value, o.value);
  }
}
