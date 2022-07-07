package org.spockframework.runtime.extension;

import org.spockframework.runtime.model.ExecutionResult;
import org.spockframework.util.Beta;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for running an iteration of a test.
 *
 * @since 2.2
 * @author Leonard Brünings
 */
@Beta
public interface IIterationRunner {
  /**
   * Runs the iteration.
   * <p>
   * The returned future can be used to wait for the iteration to complete and to get the result,
   * allowing the data driver to base the next iteration on the result of the previous one.
   * However, it is not required to wait on the futures in any way.
   *
   * @param args arguments to use for the iteration
   * @return a future that will be completed with the result of the iteration
   */
  CompletableFuture<ExecutionResult> runIteration(Object[] args);
}
