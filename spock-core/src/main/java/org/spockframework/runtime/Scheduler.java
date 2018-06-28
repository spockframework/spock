package org.spockframework.runtime;

import org.junit.runners.model.RunnerScheduler;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class Scheduler {
  private final RunnerScheduler runnerScheduler;
  private final boolean forceSequential;
  private final AtomicInteger scheduledTasksCount = new AtomicInteger(0);
  private final Semaphore concurrentJobsSemaphore = new Semaphore(0);
  private State state = State.SCHEDULING;

  public Scheduler(RunnerScheduler runnerScheduler, boolean forceSequential) {
    this.runnerScheduler = runnerScheduler;
    this.forceSequential = forceSequential;
  }

  public void schedule(final Runnable task) {
    synchronized (this){
      if (state == State.WAITING) {
        throw new IllegalStateException("No new tasks can be scheduled after waitFinished() was called");
      }
      scheduledTasksCount.incrementAndGet();
    }
    if (forceSequential) {
      task.run();
    } else {
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
    synchronized (this){
      if (state == State.WAITING) {
        throw new IllegalStateException("waitFinished() can be called only once");
      }
      state = State.WAITING;
    }
    if (!forceSequential && scheduledTasksCount.get() > 0) {
      try {
        concurrentJobsSemaphore.acquire(scheduledTasksCount.get());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Scheduler deriveScheduler(boolean forceSequential) {
    return new Scheduler(runnerScheduler, forceSequential);
  }

  private enum State {
    SCHEDULING,
    WAITING
  }
}
