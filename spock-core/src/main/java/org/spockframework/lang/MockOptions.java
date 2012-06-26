package org.spockframework.lang;

public class MockOptions {
  private boolean forceCglib;

  public boolean isForceCglib() {
    return forceCglib;
  }

  public void setForceCglib(boolean flag) {
    forceCglib = flag;
  }
}
