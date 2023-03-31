package org.spockframework.runtime.extension.builtin;

import org.spockframework.util.Beta;
import spock.config.ConfigurationObject;

/**
 * Configuration settings for the Timeout extension.
 *
 * <p>Example:
 * <pre>
 * timeout {
 *   // boolean, default true
 *   printThreadDumps true
 *   // boolean, default false
 *   captureExternalThreadDumps true
 *   // integer, default 3
 *   maxInterruptAttemptsWithThreadDump 3
 * }
 * </pre>
 *
 * @since 2.4
 */
@Beta
@ConfigurationObject("timeout")
public class TimeoutConfiguration {

  /**
   * Determines whether thread dumps will be captured and logged on feature timeout or unsuccessful interrupt attempts, default true.
   */
  public boolean printThreadDumps = true;

  /**
   * Determines whether thread dumps of external Java processes should be captured and logged,
   * default false meaning that only the thread dump of current JVM is captured and logged.
   */
  public boolean captureExternalThreadDumps = false;

  /**
   * Maximum number of unsuccessful interrupts to log the thread dumps for, default 3.
   */
  public int maxInterruptAttemptsWithThreadDump = 3;
}
