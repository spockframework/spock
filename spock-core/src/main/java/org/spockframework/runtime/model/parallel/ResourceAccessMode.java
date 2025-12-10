package org.spockframework.runtime.model.parallel;

public enum ResourceAccessMode {

  /**
   * Require read and write access to the resource.
   */
  READ_WRITE,

  /**
   * Require only read access to the resource.
   */
  READ

}
