package org.spockframework.smoke.mock;

public interface ISquare {
  int getLength();

  default int getArea() {
    return getLength() * getLength();
  }
}
