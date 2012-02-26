/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime.extension.builtin;

import java.text.DecimalFormat;
import java.util.concurrent.*;

import org.spockframework.runtime.SpockTimeoutError;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;

import spock.lang.Timeout;

/**
 * Times out a method invocation if it takes too long. The method invocation
 * will occur on the regular test framework thread. This can be important
 * for integration tests with thread-local state.
 *
 * @author Peter Niederwieser
 */

public class TimeoutInterceptor implements IMethodInterceptor {
  private final Timeout timeout;

  public TimeoutInterceptor(Timeout timeout) {
    this.timeout = timeout;
  }

  public void intercept(final IMethodInvocation invocation) throws Throwable {
    final Thread mainThread = Thread.currentThread();
    final SynchronousQueue<StackTraceElement[]> sync = new SynchronousQueue<StackTraceElement[]>();

    new Thread(String.format("[spock.lang.Timeout] Watcher for method '%s'", invocation.getMethod().getName())) {
      public void run() {
        StackTraceElement[] stackTrace = new StackTraceElement[0];
        long waitMillis = timeout.unit().toMillis(timeout.value());
        boolean synced = false;

        while (!synced) {
          try {
            synced = sync.offer(stackTrace, waitMillis, TimeUnit.MILLISECONDS);
          } catch (InterruptedException ignored) {
            // The mission of this thread is to repeatedly interrupt the main thread until
            // the latter returns. Once this mission has been accomplished, this thread will die quickly
          }
          if (!synced) {
            if (stackTrace.length == 0) {
              stackTrace = mainThread.getStackTrace();
              waitMillis = 250;
            } else {
              waitMillis *= 2;
              System.out.printf("[spock.lang.Timeout] Method '%s' has not yet returned - interrupting. Next try in %1.2f seconds.\n", invocation.getMethod().getName(), waitMillis / 1000.);
            }
            mainThread.interrupt();
          }
        }
      }
    }.start();

    Throwable saved = null;
    try {
      invocation.proceed();
    } catch (Throwable t) {
      saved = t;
    }
    StackTraceElement[] stackTrace = null;
    while (stackTrace == null) {
      try {
        stackTrace = sync.take();
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
    if (stackTrace.length > 0) {
      // We know that this thread got timed out (and interrupted) by the watcher thread and
      // act accordingly. We gloss over the fact that some other thread might also have tried to
      // interrupt this thread. This shouldn't be a problem in practice, in particular because
      // throwing an InterruptedException wouldn't abort the whole test run anyway.
      SpockTimeoutError timeoutError = new SpockTimeoutError(timeout.value(), timeout.unit(),
          "Method timed out after %d %s", timeout.value(), timeout.unit().toString().toLowerCase());
      timeoutError.setStackTrace(stackTrace);
      throw timeoutError;
    }
    if (saved != null) {
      throw saved;
    }
  }
}