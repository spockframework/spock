package org.spockframework.runtime.model;

import java.util.HashSet;
import java.util.Set;

public class IterationFilter {

  private final Set<Integer> allowed = new HashSet<>();
  private Mode mode = Mode.EXPLICIT;

  public void allow(int index) {
    if (this.mode == Mode.EXPLICIT) {
      this.allowed.add(index);
    }
  }

  public void allowAll() {
    this.mode = Mode.ALLOW_ALL;
    this.allowed.clear();
  }

  public boolean isAllowed(int index) {
    return allowed.isEmpty() || allowed.contains(index);
  }

  private enum Mode {
    EXPLICIT, ALLOW_ALL
  }
}
