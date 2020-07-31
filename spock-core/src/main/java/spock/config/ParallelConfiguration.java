package spock.config;

import org.spockframework.runtime.model.parallel.ExecutionMode;

public class ParallelConfiguration {

  public boolean enabled = Boolean.getBoolean("spock.parallel.enabled");
  public ExecutionMode defaultClassesExecutionMode = ExecutionMode.CONCURRENT;
  public ExecutionMode defaultExecutionMode = ExecutionMode.CONCURRENT;
}
