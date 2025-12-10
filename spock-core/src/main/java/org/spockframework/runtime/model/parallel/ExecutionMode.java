package org.spockframework.runtime.model.parallel;

/**
 * Supported execution modes for parallel test execution.
 *
 * @since 2.0
 */
public enum ExecutionMode {

  /**
   * Force execution in same thread as the parent node.
   *
   * @see #CONCURRENT
   */
  SAME_THREAD,

  /**
   * Allow concurrent execution with any other node.
   *
   * @see #SAME_THREAD
   */
  CONCURRENT
}
