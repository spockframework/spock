package org.spockframework.junit.scheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.runner.Computer;
import org.junit.runner.Runner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.RunnerScheduler;

public class ParallelComputerWithFixedPool extends Computer {
  private final int threadCount;

  public ParallelComputerWithFixedPool(int threadCount) {
    this.threadCount = threadCount;
  }

  private Runner parallelize(Runner runner) {
    if (runner instanceof ParentRunner) {
      ((ParentRunner<?>) runner).setScheduler(new RunnerScheduler() {
        private final ExecutorService fService = Executors.newFixedThreadPool(threadCount);

        public void schedule(Runnable childStatement) {
          fService.submit(childStatement);
        }

        public void finished() {
          try {
            fService.shutdown();
            fService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
          } catch (InterruptedException e) {
            e.printStackTrace(System.err);
          }
        }
      });
    }
    return runner;
  }

  @Override
  public Runner getSuite(RunnerBuilder builder, java.lang.Class<?>[] classes)
    throws InitializationError {
    Runner suite = super.getSuite(builder, classes);
    return parallelize(suite);
  }

  @Override
  protected Runner getRunner(RunnerBuilder builder, Class<?> testClass)
    throws Throwable {
    Runner runner = super.getRunner(builder, testClass);
    return parallelize(runner);
  }
}
