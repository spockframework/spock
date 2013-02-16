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

import java.io.PrintStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.spockframework.util.StringMessagePrintStream;
import org.spockframework.util.TeePrintStream;

public class StandardStreamsCapturer {
  private final Set<IStandardStreamsListener> standardStreamsListeners =
    new CopyOnWriteArraySet<IStandardStreamsListener>();

  public void install() {
    PrintStream out = System.out;
    if (!(out instanceof MyTeePrintStream)) {
      StringMessagePrintStream stream = new StringMessagePrintStream() {
        @Override
        protected void printed(String message) {
          for (IStandardStreamsListener listener : standardStreamsListeners) {
            listener.standardOut(message);
          }
        }
      };
      System.setOut(new MyTeePrintStream(out, stream));
    }

    PrintStream err = System.err;
    if (!(err instanceof MyTeePrintStream)) {
      StringMessagePrintStream stream = new StringMessagePrintStream() {
        @Override
        protected void printed(String message) {
          for (IStandardStreamsListener listener : standardStreamsListeners) {
            listener.standardErr(message);
          }
        }
      };
      System.setErr(new MyTeePrintStream(err, stream));
    }
  }

  public void uninstall() {
    PrintStream out = System.out;
    if (out instanceof MyTeePrintStream) {
      System.setOut(((MyTeePrintStream) out).getDelegates().get(0));
    }

    PrintStream err = System.err;
    if (err instanceof MyTeePrintStream) {
      System.setErr(((MyTeePrintStream) err).getDelegates().get(0));
    }
  }

  public void addStandardStreamsListener(IStandardStreamsListener listener) {
    standardStreamsListeners.add(listener);
  }

  public void removeStandardStreamsListener(IStandardStreamsListener listener) {
    standardStreamsListeners.remove(listener);
  }

  private static class MyTeePrintStream extends TeePrintStream {
    MyTeePrintStream(PrintStream... delegates) {
      super(delegates);
    }
  }
}
