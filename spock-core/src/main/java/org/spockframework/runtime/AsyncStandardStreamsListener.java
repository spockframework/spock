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

public class AsyncStandardStreamsListener extends AsyncRunListener implements IStandardStreamsListener {
  private final IStandardStreamsListener streamsDelegate;

  public AsyncStandardStreamsListener(String threadName, IRunListener delegate, IStandardStreamsListener streamsDelegate) {
    super(threadName, delegate);
    this.streamsDelegate = streamsDelegate;
  }

  public void standardOut(final String message) {
    addEvent(new Runnable() {
      public void run() {
        streamsDelegate.standardOut(message);
      }
    });
  }

  public void standardErr(final String message) {
    addEvent(new Runnable() {
      public void run() {
        streamsDelegate.standardErr(message);
      }
    });
  }
}
