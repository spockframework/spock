package org.spockframework.runtime;

import org.junit.runners.model.RunnerScheduler;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Scheduler {
  private final RunnerScheduler runnerScheduler;
  private final boolean forceSequential;
  private final AtomicInteger scheduledConcurrentTasks = new AtomicInteger(0);
  private final Semaphore concurrentJobsSemaphore = new Semaphore(0);

  public Scheduler(RunnerScheduler runnerScheduler, boolean forceSequential) {
    this.runnerScheduler = runnerScheduler;
    this.forceSequential = forceSequential;
  }

  public void schedule(final Runnable task) {
    if (forceSequential) {
      task.run();
    } else {
      scheduledConcurrentTasks.incrementAndGet();
      runnerScheduler.schedule(new Runnable() {
        @Override
        public void run() {
          try {
            task.run();
          } finally {
            concurrentJobsSemaphore.release();
          }
        }
      });
    }
  }

  public void waitFinished() {
    if (!forceSequential && scheduledConcurrentTasks.get() > 0) {
      try {
        concurrentJobsSemaphore.acquire(scheduledConcurrentTasks.get());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Scheduler deriveScheduler(boolean forceSequential) {
    return new Scheduler(runnerScheduler, forceSequential);
  }
}
