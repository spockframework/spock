/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.SpockTimeoutError;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.util.*;
import spock.lang.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Times out a method invocation if it takes too long. The method invocation
 * will occur on the regular test framework thread. This can be important
 * for integration tests with thread-local state.
 *
 * @author Peter Niederwieser
 */

public class TimeoutInterceptor implements IMethodInterceptor {

  private final Timeout timeout;
  private final TimeoutConfiguration configuration;
  private final JavaProcessThreadDumpCollector threadDumpCollector;

  public TimeoutInterceptor(Timeout timeout, TimeoutConfiguration configuration) {
    this.timeout = timeout;
    this.configuration = configuration;
    this.threadDumpCollector = JavaProcessThreadDumpCollector.create(configuration.threadDumpUtilityType);
  }

  @Override
  public void intercept(final IMethodInvocation invocation) throws Throwable {
    final Thread mainThread = Thread.currentThread();
    final SynchronousQueue<Boolean> sync = new SynchronousQueue<>();
    final CountDownLatch startLatch = new CountDownLatch(2);
    final String methodName = invocation.getMethod().getName();
    final double timeoutSeconds = TimeUtil.toSeconds(timeout.value(), timeout.unit());

    new Thread(String.format("[spock.lang.Timeout] Watcher for method '%s'", methodName)) {
      @Override
      public void run() {
        boolean timedOut = false;
        long waitMillis = timeout.unit().toMillis(timeout.value());
        boolean synced = false;
        long timeoutAt = 0;
        int unsuccessfulInterruptAttempts = 0;

        syncWithThread(startLatch, "feature", methodName);

        while (!synced) {
          try {
            synced = sync.offer(timedOut, waitMillis, TimeUnit.MILLISECONDS);
          } catch (InterruptedException ignored) {
            // The mission of this thread is to repeatedly interrupt the main thread until
            // the latter returns. Once this mission has been accomplished, this thread will die quickly
          }
          if (!synced) {
            long now = System.nanoTime();
            if (!timedOut) {
              logMethodTimeout(methodName, timeoutSeconds);
              timedOut = true;
              waitMillis = 250;
              timeoutAt = now;
            } else {
              waitMillis *= 2;
              logUnsuccessfulInterrupt(methodName, now, timeoutAt, waitMillis, ++unsuccessfulInterruptAttempts);
            }
            mainThread.interrupt();
          }
        }
      }
    }.start();

    syncWithThread(startLatch, "watcher", methodName);

    Throwable saved = null;
    try {
      invocation.proceed();
    } catch (Throwable t) {
      saved = t;
    }
    Boolean timedOut = null;
    while (timedOut == null) {
      try {
        timedOut = sync.take();
      } catch (InterruptedException e) {
        // There is a small chance that this came from the watcher thread,
        // i.e. the two threads narrowly missed each other at the sync point.
        // Therefore, let's sync again to learn whether this thread has timed out or not.
        // As this won't take long, it should also be acceptable if this thread
        // got interrupted by some other thread. To report on the latter case,
        // we save off the exception, overriding any previously saved exception.
        saved = e;
      }
    }
    if (timedOut) {
      // We know that this thread got timed out (and interrupted) by the watcher thread and
      // act accordingly. We gloss over the fact that some other thread might also have tried to
      // interrupt this thread. This shouldn't be a problem in practice, in particular because
      // throwing an InterruptedException wouldn't abort the whole test run anyway.
      String msg = String.format("Method timed out after %1.2f seconds", timeoutSeconds);
      SpockTimeoutError error = new SpockTimeoutError(timeoutSeconds, msg);

      // The trace was already logged when timeout occurred, so don't repeat it
      error.setStackTrace(new StackTraceElement[]{});
      throw error;
    }
    if (saved != null) {
      throw saved;
    }
  }

  private void logUnsuccessfulInterrupt(String methodName, long now, long timeoutAt, long waitMillis, int unsuccessfulAttempts) {
    System.err.printf(
      "[spock.lang.Timeout] Method '%s' has not stopped after timing out %1.2f seconds ago - interrupting. Next try in %1.2f seconds.\n%n",
      methodName,
      Duration.ofNanos(now - timeoutAt).toMillis() / 1000.,
      waitMillis / 1000.
    );

    if (unsuccessfulAttempts <= configuration.maxInterruptAttemptsWithThreadDumps) {
      logThreadDumpOfCurrentJvm(configuration.printThreadDumpsOnInterruptAttempts);
      configuration.interruptAttemptListeners.forEach(Runnable::run);

      if (unsuccessfulAttempts == configuration.maxInterruptAttemptsWithThreadDumps) {
        System.out.println("[spock.lang.Timeout] No further thread dumps will be logged and no timeout listeners will be run, as the number of unsuccessful interrupt attempts exceeds configured maximum of logged attempts");
      }
    }
  }

  private void logMethodTimeout(String methodName, double timeoutSeconds) {
    System.err.printf(
      "[spock.lang.Timeout] Method '%s' timed out after %1.2f seconds.%n",
      methodName,
      timeoutSeconds
    );

    logThreadDumpOfCurrentJvm(true);
    configuration.interruptAttemptListeners.forEach(Runnable::run);
  }

  private void logThreadDumpOfCurrentJvm(boolean printThreadDump) {
    if (printThreadDump) {
      StringBuilder sb = new StringBuilder();
      try {
        threadDumpCollector.appendThreadDumpOfCurrentJvm(sb);
        System.err.println(removeThisThread(sb.toString()));
      } catch (Throwable e) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(stream));

        String result = "Error in attempt to fetch thread dumps: " + stream;
        if (sb.length() > 0) {
          result += "\n\nPartial thread dumps:\n" + sb;
        }
        System.err.println(result);
      }
    }
  }

  private static String removeThisThread(String jcmdThreadDumpOutput) {
    Thread thisThread = Thread.currentThread();
    String threadName = thisThread.getName();
    long threadId = thisThread.getId();

    List<String> lines = Arrays.asList(jcmdThreadDumpOutput.split("\n"));
    Pair<Integer, Integer> thisThreadSection = findThreadSection(lines, threadName, threadId);
    if (thisThreadSection == null) {
      return jcmdThreadDumpOutput;
    }

    String start = TextUtil.join("\n", lines.subList(0, thisThreadSection.first()));
    int thisThreadSectionStop = thisThreadSection.second();
    if (thisThreadSectionStop == lines.size()) {
      return start;
    } else {
      return start + TextUtil.join("\n", lines.subList(thisThreadSectionStop + 1, lines.size() - 1));
    }
  }

  @Nullable
  private static Pair<Integer, Integer> findThreadSection(List<String> lines, String threadName, long threadId) {
    int threadSectionStart = -1;
    for (int i = 0; i < lines.size(); ++i) {
      if (lines.get(i).startsWith(String.format("\"%s\" #%d", threadName, threadId))) {
        threadSectionStart = i;
      } else if (threadSectionStart > 0 && lines.get(i).isEmpty()) {
        return Pair.of(threadSectionStart, i);
      }
    }

    return null;
  }

  private static void syncWithThread(CountDownLatch startLatch, String threadName, String methodName) {
    try {
      startLatch.countDown();
      startLatch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException ignored) {
      System.out.printf("[spock.lang.Timeout] Could not sync with %s thread for method '%s'", threadName, methodName);
    }
  }
}
