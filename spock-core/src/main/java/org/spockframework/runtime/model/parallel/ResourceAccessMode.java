package org.spockframework.runtime.model.parallel;

import org.spockframework.util.Beta;

@Beta
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
