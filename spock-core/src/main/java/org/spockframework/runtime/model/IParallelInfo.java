package org.spockframework.runtime.model;

import org.spockframework.runtime.model.parallel.*;

import java.util.*;

public interface IParallelInfo {
  void addExclusiveResource(ExclusiveResource exclusiveResource);

  Set<ExclusiveResource> getExclusiveResources();

  void setExecutionMode(ExecutionMode executionMode);

  Optional<ExecutionMode> getExecutionMode();
}
