package org.spockframework.mock.codegen;

/**
 * Serves as a reference point for method handle based class loading and is makes the containing package
 * non-empty so generated classes can be legally defined inside of it.
 */
public class Target {
  private Target() {
    // never called
  }
}
