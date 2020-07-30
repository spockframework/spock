package spock.config;

import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;

public class ParallelConfiguration implements ParallelExecutionConfiguration {

  public boolean enabled = Boolean.getBoolean("spock.parallel.enabled");
  int parallelism;
  int minimumRunnable;
  int maxPoolSize;
  int corePoolSize;
  int keepAliveSeconds;

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
