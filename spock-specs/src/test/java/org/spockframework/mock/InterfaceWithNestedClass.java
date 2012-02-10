package org.spockframework.mock;

public interface InterfaceWithNestedClass {
  class Service {
    private Service() {}

    public static InterfaceWithNestedClass getInstance() {
      return null;
    }
  }
}
