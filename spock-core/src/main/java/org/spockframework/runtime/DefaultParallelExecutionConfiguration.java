package org.spockframework.runtime;

import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;

public class DefaultParallelExecutionConfiguration implements ParallelExecutionConfiguration {

  private final int parallelism;
  private final int minimumRunnable;
  private final int maxPoolSize;
  private final int corePoolSize;
  private final int keepAliveSeconds;

  public DefaultParallelExecutionConfiguration(int parallelism, int minimumRunnable, int maxPoolSize, int corePoolSize,
    int keepAliveSeconds) {
    this.parallelism = parallelism;
    this.minimumRunnable = minimumRunnable;
    this.maxPoolSize = maxPoolSize;
    this.corePoolSize = corePoolSize;
    this.keepAliveSeconds = keepAliveSeconds;
  }

  @Override
  public int getParallelism() {
    return parallelism;
  }

  @Override
  public int getMinimumRunnable() {
    return minimumRunnable;
  }

  @Override
  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  @Override
  public int getCorePoolSize() {
    return corePoolSize;
  }

  @Override
  public int getKeepAliveSeconds() {
    return keepAliveSeconds;
  }

}
