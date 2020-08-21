package spock.config;

import java.math.BigDecimal;

import org.spockframework.runtime.model.parallel.ExecutionMode;

public class ParallelConfiguration {
  private static final int KEEP_ALIVE_SECONDS = 30;

  private static final int MAX_POOL_SIZE_BASE = 256;

  public boolean enabled = Boolean.getBoolean("spock.parallel.enabled");

  public ExecutionMode defaultClassesExecutionMode = ExecutionMode.CONCURRENT;

  public ExecutionMode defaultExecutionMode = ExecutionMode.CONCURRENT;

  /**
   * Computes the desired parallelism based on the number of available
   * processors/cores multiplied by the {@code factor}.
   *
   * @param factor to use to determine parallelism
   */
  public void dynamic(BigDecimal factor) {
    int parallelism = Math.max(1,
      factor.multiply(BigDecimal.valueOf(Runtime.getRuntime().availableProcessors())).intValue());
    fixed(parallelism);
  }

  /**
   * Uses a fixed value for {@code parallelism}.
   *
   * @param parallelism the parallelism level
   */
  public void fixed(int parallelism) {
    custom(parallelism, parallelism, MAX_POOL_SIZE_BASE + parallelism, parallelism, KEEP_ALIVE_SECONDS);
  }

  /**
   * Allows fine grained custom control over the parameters of the execution.
   *
   * @param parallelism the parallelism level
   * @param minimumRunnable the minimum allowed number of core
   * threads not blocked by a join or {@code ManagedBlocker}.  To
   * ensure progress, when too few unblocked threads exist and
   * unexecuted tasks may exist, new threads are constructed, up to
   * the given maximumPoolSize.  For the default value, use {@code
   * 1}, that ensures liveness.  A larger value might improve
   * throughput in the presence of blocked activities, but might
   * not, due to increased overhead.  A value of zero may be
   * acceptable when submitted tasks cannot have dependencies
   * requiring additional threads.
   * @param maxPoolSize the maximum number of threads allowed.
   * When the maximum is reached, attempts to replace blocked
   * threads fail.  (However, because creation and termination of
   * different threads may overlap, and may be managed by the given
   * thread factory, this value may be transiently exceeded.)  To
   * arrange the same value as is used by default for the common
   * pool, use {@code 256} plus the {@code parallelism} level. (By
   * default, the common pool allows a maximum of 256 spare
   * threads.)  Using a value (for example {@code
   * Integer.MAX_VALUE}) larger than the implementation's total
   * thread limit has the same effect as using this limit (which is
   * the default).
   * @param corePoolSize the number of threads to keep in the pool
   * (unless timed out after an elapsed keep-alive). Normally (and
   * by default) this is the same value as the parallelism level,
   * but may be set to a larger value to reduce dynamic overhead if
   * tasks regularly block. Using a smaller value (for example
   * {@code 0}) has the same effect as the default.
   * @param keepAliveSeconds the elapsed time since last use before
   * a thread is terminated (and then later replaced if needed).
   */
  public void custom(int parallelism, int minimumRunnable, int maxPoolSize, int corePoolSize, int keepAliveSeconds) {
  }
}
