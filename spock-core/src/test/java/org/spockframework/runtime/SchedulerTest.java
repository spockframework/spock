package org.spockframework.runtime;

import org.junit.Test;
import org.junit.runners.model.RunnerScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SchedulerTest {

  @Test
  public void testSequentialParent() throws Exception {
    final SequentialRunnerScheduler sequentialRunnerScheduler = new SequentialRunnerScheduler();
    assertThatTasksProcessedSequentially(new Scheduler(sequentialRunnerScheduler, false));
    assertThatTasksProcessedSequentially(new Scheduler(sequentialRunnerScheduler, true));
    assertThatTasksProcessedSequentially(new Scheduler(sequentialRunnerScheduler, true).deriveScheduler(false));
    assertThatTasksProcessedSequentially(new Scheduler(sequentialRunnerScheduler, true).deriveScheduler(true));
  }

  @Test
  public void testParallelParent() throws Exception {
    final ParallelRunnerScheduler sequentialRunnerScheduler = new ParallelRunnerScheduler();

    assertThatTasksProcessedInParallel(new Scheduler(sequentialRunnerScheduler, false));
    assertThatTasksProcessedInParallel(new Scheduler(sequentialRunnerScheduler, true).deriveScheduler(false));

    assertThatTasksProcessedSequentially(new Scheduler(sequentialRunnerScheduler, true));
    assertThatTasksProcessedSequentially(new Scheduler(sequentialRunnerScheduler, true).deriveScheduler(true));
  }

  private void assertThatTasksProcessedSequentially(Scheduler scheduler) {
    final AtomicReference<Runnable> currentTask = new AtomicReference<Runnable>();
    final AtomicBoolean sequentialExecutionWasFailed = new AtomicBoolean(false);
    final AtomicInteger executedTaskCount = new AtomicInteger(0);

    class Task implements Runnable {
      @Override
      public void run() {
        currentTask.set(this);
        sleep(200);
        if (currentTask.get() != this) {
          sequentialExecutionWasFailed.set(true);
        }
        sleep(200);
        executedTaskCount.incrementAndGet();
      }
    }

    for (int i = 0; i < 5; i++) {
      scheduler.schedule(new Task());
    }

    scheduler.waitFinished();

    assertFalse("should be executed sequentially", sequentialExecutionWasFailed.get());
    assertEquals("all tasks should be executed", 5, executedTaskCount.get());
  }

  private void assertThatTasksProcessedInParallel(Scheduler scheduler) {
    final int taskCount = 5;

    final AtomicBoolean parallelExecutionWasFailed = new AtomicBoolean(false);
    final AtomicInteger executedTaskCount = new AtomicInteger(0);
    final AtomicInteger startedTaskCount = new AtomicInteger(0);

    class Task implements Runnable {
      @Override
      public void run() {
        startedTaskCount.incrementAndGet();
        sleep(200);
        if (startedTaskCount.get() != taskCount) { // all tasks should be being executed
          parallelExecutionWasFailed.set(true);
        }
        sleep(200);
        executedTaskCount.incrementAndGet();
      }
    }

    for (int i = 0; i < taskCount; i++) {
      scheduler.schedule(new Task());
    }

    scheduler.waitFinished();

    assertFalse("should be executed in parallel", parallelExecutionWasFailed.get());
    assertEquals("all tasks should be executed", taskCount, executedTaskCount.get());
  }


  private void sleep(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }


  //------------------------------------------------------------------------

  private static class ParallelRunnerScheduler implements RunnerScheduler {
    private final ExecutorService fService = Executors.newCachedThreadPool();

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
  }
}