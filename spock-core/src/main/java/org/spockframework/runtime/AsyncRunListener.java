/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import org.spockframework.runtime.model.*;
import org.spockframework.util.IStoppable;

import java.util.concurrent.*;

public class AsyncRunListener implements IRunListener, IStoppable {
  private static final Runnable STOP = new Runnable() {
    @Override
    public void run() {
      throw new IllegalStateException("should never run");
    }
  };

  private final IRunListener delegate;
  private final Thread workerThread;
  private final BlockingQueue<Runnable> events = new LinkedBlockingQueue<>();
  private volatile boolean stopped = false;

  public AsyncRunListener(String threadName, IRunListener delegate) {
    this.delegate = delegate;
    this.workerThread = new Thread(threadName) {
      @Override
      public void run() {
        while(true) {
          try {
            Runnable event = events.take();
            if (event == STOP) return;
            event.run();
          } catch (Throwable t) {
            stopped = true;
            t.printStackTrace();
            return;
          }
        }
      }
    };
  }

  public void start() {
    workerThread.start();
  }

  @Override
  public void stop() throws InterruptedException {
    addEvent(STOP);
    workerThread.join();
  }

  @Override
  public void beforeSpec(final SpecInfo spec) {
    addEvent(new Runnable() {
      @Override
      public void run() {
        delegate.beforeSpec(spec);
      }
    });
  }

  @Override
  public void beforeFeature(final FeatureInfo feature) {
    addEvent(new Runnable() {
      @Override
      public void run() {
        delegate.beforeFeature(feature);
      }
    });
  }

  @Override
  public void beforeIteration(final IterationInfo iteration) {
    addEvent(new Runnable() {
      @Override
      public void run() {
        delegate.beforeIteration(iteration);
      }
    });
  }

  @Override
  public void afterIteration(final IterationInfo iteration) {
    addEvent(new Runnable() {
      @Override
      public void run() {
        delegate.afterIteration(iteration);
      }
    });
  }

  @Override
  public void afterFeature(final FeatureInfo feature) {
    addEvent(new Runnable() {
      @Override
      public void run() {
        delegate.afterFeature(feature);
      }
    });
  }

  @Override
  public void afterSpec(final SpecInfo spec) {
    addEvent(new Runnable() {
      @Override
      public void run() {
        delegate.afterSpec(spec);
      }
    });
  }

  @Override
  public void error(final ErrorInfo error) {
    addEvent(new Runnable() {
      @Override
      public void run() {
        delegate.error(error);
      }
    });
  }

  @Override
  public void specSkipped(final SpecInfo spec) {
    addEvent(new Runnable() {
      @Override
      public void run() {
        delegate.specSkipped(spec);
      }
    });
  }

  @Override
  public void featureSkipped(final FeatureInfo feature) {
    addEvent(new Runnable() {
      @Override
      public void run() {
        delegate.featureSkipped(feature);
      }
    });
  }

  protected void addEvent(Runnable event) {
    if (stopped) return;
    events.add(event);
  }
}
