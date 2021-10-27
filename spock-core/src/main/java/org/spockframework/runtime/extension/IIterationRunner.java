package org.spockframework.runtime.extension;

import org.spockframework.runtime.model.ExecutionResult;

import java.util.concurrent.CompletableFuture;

public interface IIterationRunner {
  CompletableFuture<ExecutionResult> runIteration(Object[] args);
}
