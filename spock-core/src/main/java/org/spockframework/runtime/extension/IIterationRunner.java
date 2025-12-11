package org.spockframework.runtime.extension;

import java.util.concurrent.CompletableFuture;

import org.spockframework.runtime.model.ExecutionResult;

import static org.spockframework.runtime.DataIteratorFactory.UNKNOWN_ITERATIONS;

/**
 * Interface for running an iteration of a test.
 *
 * @since 2.2
 * @author Leonard Brünings
 */
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
   * @deprecated since 2.4, use {@link #runIteration(Object[], int)} instead
   */
  @Deprecated
  default CompletableFuture<ExecutionResult> runIteration(Object[] args) {
    return runIteration(args, UNKNOWN_ITERATIONS);
  }

  /**
   * Runs the iteration.
   * <p>
   * The returned future can be used to wait for the iteration to complete and to get the result,
   * allowing the data driver to base the next iteration on the result of the previous one.
   * However, it is not required to wait on the futures in any way.
   *
   * @param args arguments to use for the iteration
   * @param estimatedNumIterations the estimated number of iterations that will be run. Use -1 if it cannot be determined.
   * @return a future that will be completed with the result of the iteration
   * @since 2.4
   */
  CompletableFuture<ExecutionResult> runIteration(Object[] args, int estimatedNumIterations);
}
