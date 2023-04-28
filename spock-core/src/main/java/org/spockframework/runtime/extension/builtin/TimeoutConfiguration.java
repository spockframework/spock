package org.spockframework.runtime.extension.builtin;

import org.spockframework.util.Beta;
import spock.config.ConfigurationObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration settings for the Timeout extension.
 *
 * <p>Example:
 * <pre>
 * timeout {
 *   // boolean, default true
 *   printThreadDump true
 *   // integer, default 3
 *   maxInterruptAttemptsWithThreadDump 5
 *   // ThreadDumpCapturingUtil, default JCMD
 *   threadDumpCapturingUtil ThreadDumpCapturingUtil.JSTACK
 *   // list of java.lang.Runnable, default []
 *   onTimeoutListeners.add({ println('Timeout occurred!') })
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
  public boolean printThreadDump = true;

  /**
   * Maximum number of unsuccessful interrupts to log the thread dumps for, default 3.
   */
  public int maxInterruptAttemptsWithThreadDump = 3;

  /**
   * Utility used to capture thread dumps, default {@link ThreadDumpCapturingUtil#JCMD}.
   */
  public ThreadDumpCapturingUtil threadDumpCapturingUtil = ThreadDumpCapturingUtil.JCMD;

  /**
   * Listeners to be invoked on method timeout or unsuccessful interrupt attempts, default empty.
   */
  public List<Runnable> onTimeoutListeners = new ArrayList<>();
}