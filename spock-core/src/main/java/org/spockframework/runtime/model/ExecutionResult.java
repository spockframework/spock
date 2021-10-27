package org.spockframework.runtime.model;

public enum ExecutionResult {

  /**
   * Indicates that the execution of a test or container was
   * <em>successful</em>.
   */
  SUCCESSFUL,

  /**
   * Indicates that the execution of a test or container was
   * <em>aborted</em> (started but not finished).
   */
  ABORTED,

  /**
   * Indicates that the execution of a test or container <em>failed</em>.
   */
  FAILED,

  /**
   * Indicates that the execution of a test or container was rejected due to a previous failure.
   * No further executions should be triggered when this status is returned.
   */
  REJECTED
}
