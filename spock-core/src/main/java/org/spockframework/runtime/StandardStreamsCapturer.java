/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import org.spockframework.util.*;

import java.io.PrintStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ThreadSafe
public class StandardStreamsCapturer implements IStoppable {
  private final Set<IStandardStreamsListener> standardStreamsListeners =
    new CopyOnWriteArraySet<>();

  private volatile TeePrintStream outStream;
  private volatile TeePrintStream errStream;

  public synchronized void start() {
    startCapture(System.out, outStream, true);
    startCapture(System.err, errStream, false);
  }

  private void startCapture(PrintStream originalStream, TeePrintStream teeStream, final boolean isOut) {
    if (originalStream == teeStream) return;

    StringMessagePrintStream notifyingStream = new StringMessagePrintStream() {
      @Override
      protected void printed(String message) {
        for (IStandardStreamsListener listener : standardStreamsListeners) {
          if (isOut) {
            listener.standardOut(message);
          } else {
            listener.standardErr(message);
          }
        }
      }
    };

    teeStream = new TeePrintStream(originalStream, notifyingStream);
    if (isOut) {
      outStream = teeStream;
      System.setOut(teeStream);
    } else {
      errStream = teeStream;
      System.setErr(teeStream);
    }
  }

  @Override
  public synchronized void stop() {
    stopCapture(System.out, outStream, true);
    stopCapture(System.err, errStream, false);
  }

  private void stopCapture(PrintStream originalStream, TeePrintStream teeStream, boolean isOut) {
    if (originalStream != teeStream) return;
    if (isOut) {
      System.setOut(teeStream.getOriginal());
    } else {
      System.setErr(teeStream.getOriginal());
    }
  }

  public synchronized void muteStandardStreams() {
    if (outStream != null) {
      outStream.muteOriginal();
    }
    if (errStream != null) {
      errStream.muteOriginal();
    }
  }

  public synchronized void unmuteStandardStreams() {
    if (outStream != null) {
      outStream.unmuteOriginal();
    }
    if (errStream != null) {
      errStream.unmuteOriginal();
    }
  }

  public void addStandardStreamsListener(IStandardStreamsListener listener) {
    standardStreamsListeners.add(listener);
  }

  public void removeStandardStreamsListener(IStandardStreamsListener listener) {
    standardStreamsListeners.remove(listener);
  }
}
