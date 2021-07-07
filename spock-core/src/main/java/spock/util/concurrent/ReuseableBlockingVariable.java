package spock.util.concurrent;


import org.spockframework.runtime.SpockTimeoutError;
import org.spockframework.util.ThreadSafe;
import org.spockframework.util.TimeUtil;

import java.util.concurrent.TimeUnit;

/**
 * Behaves like <tt>BlockingVariable</tt> except that it can be reset
 * to it's initial state.
 *
 * <p>Example:
 * <pre>
 *
 * def result = new ReuseableBlockingVariable&lt;WorkResult&gt;
 *
 * // register async callback
 * queueListener.receive << { r ->
 *  result.set(r)
 * }
 *
 * when:
 * queue.send("a")
 *
 * then:
 * // blocks until workDone callback has set result, or a timeout expires
 * result.get() == "a"
 *
 * when:
 * // resets the internal state to ensure that successive get's wait for a new value to be set
 * result.reset()
 * queue.send("a")
 *
 * then:
 * // blocks until workDone callback has set result, or a timeout expires
 * result.get() == "b"
 * </pre>
 *
 * @see BlockingVariable
 * @author Günther Grill
 */
@ThreadSafe
public class ReuseableBlockingVariable<T> {
  private static final int DEFAULT_TIMEOUT_SEC = 1;

  private final double timeout;
  private BlockingVariable<T> blockingVariable;

  /**
   * Instantiates a <tt>ReuseableBlockingVariable</tt> with a timeout of one second.
   */
  public ReuseableBlockingVariable() {
    this(DEFAULT_TIMEOUT_SEC);
  }

  /**
   * Instantiates a <tt>ReuseableBlockingVariable</tt> with the specified timeout in seconds.
   *
   * @param timeout the timeout (in seconds) for calls to <tt>get()</tt>.
   */
  public ReuseableBlockingVariable(double timeout) {
    this.timeout = timeout;
    this.blockingVariable = new BlockingVariable<T>(timeout);
  }

  /**
   * Instantiates a <tt>ReuseableBlockingVariable</tt> with the specified timeout.
   *
   * @param timeout the timeout for calls to <tt>get()</tt>.
   * @param unit the time unit
   */
  public ReuseableBlockingVariable(long timeout, TimeUnit unit) {
    this(TimeUtil.toSeconds(timeout, unit));
  }

  /**
   * Returns the timeout (in seconds).
   *
   * @return the timeout (in seconds)
   */
  public double getTimeout() {
    return timeout;
  }

  /**
   * Blocks until a value has been set for this variable, or a timeout expires.
   * The wait process won't be interrupted when the <tt>rest</tt> is called.
   *
   * @return the variable's value
   *
   * @throws SpockTimeoutError if the timeout is reached
   * @throws InterruptedException if the calling thread is interrupted
   */
  public T get() throws InterruptedException {
    return blockingVariable.get();
  }

  /**
   * Sets a value for this variable. Wakes up all threads blocked in <tt>get()</tt>.
   *
   *
   * @param value the value to be set for this variable
   */
  public void set(T value) {
    blockingVariable.set(value);
  }

  /**
   * Resets this <tt>ReuseableBlockingVariable</tt> to its initial state. That affects all
   * operations currently working on this object.
   */
  public void reset() {
    blockingVariable = new BlockingVariable<T>();
  }
}
