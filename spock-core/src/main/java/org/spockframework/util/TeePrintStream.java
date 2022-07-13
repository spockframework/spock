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

package org.spockframework.util;

import org.spockframework.runtime.GroovyRuntimeUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import groovy.lang.MissingMethodException;

import static java.util.Arrays.asList;

@ThreadSafe
public class TeePrintStream extends PrintStream {
  private final List<PrintStream> delegates;

  public TeePrintStream(PrintStream... delegates) {
    this(asList(delegates));
  }

  public TeePrintStream(List<PrintStream> delegates) {
    super(new ByteArrayOutputStream(0));
    this.delegates = new CopyOnWriteArrayList<>(delegates);
  }

  public List<PrintStream> getDelegates() {
    return delegates;
  }

  public void stopDelegation() {
    delegates.clear();
  }

  @Override
  public void flush() {
    for (PrintStream stream : delegates) {
      stream.flush();
    }
  }

  @Override
  public void close() {
    for (PrintStream stream : delegates) {
      stream.close();
    }
  }

  @Override
  public boolean checkError() {
    for (PrintStream stream : delegates) {
      if (stream.checkError()) return true;
    }
    return false;
  }

  @Override
  protected void setError() {
    throw new UnsupportedOperationException("setError");
  }

  @Override
  protected void clearError() {
    for (PrintStream stream : delegates) {
      try {
        GroovyRuntimeUtil.invokeMethod(stream, "clearError");
      } catch (MissingMethodException e) {
        // method doesn't exist in JDK 1.5
        if (!"clearError".equals(e.getMethod())) {
          throw e;
        }
      }
    }
  }

  @Override
  public void write(int b) {
    for (PrintStream stream : delegates) {
      stream.write(b);
    }
  }

  @Override
  public void write(byte[] buf, int off, int len) {
    for (PrintStream stream : delegates) {
      stream.write(buf, off, len);
    }
  }

  @Override
  public void print(boolean b) {
    for (PrintStream stream : delegates) {
      stream.print(b);
    }
  }

  @Override
  public void print(char c) {
    for (PrintStream stream : delegates) {
      stream.print(c);
    }
  }

  @Override
  public void print(int i) {
    for (PrintStream stream : delegates) {
      stream.print(i);
    }
  }

  @Override
  public void print(long l) {
    for (PrintStream stream : delegates) {
      stream.print(l);
    }
  }

  @Override
  public void print(float f) {
    for (PrintStream stream : delegates) {
      stream.print(f);
    }
  }

  @Override
  public void print(double d) {
    for (PrintStream stream : delegates) {
      stream.print(d);
    }
  }

  @Override
  public void print(char[] s) {
    for (PrintStream stream : delegates) {
      stream.print(s);
    }
  }

  @Override
  public void print(String s) {
    for (PrintStream stream : delegates) {
      stream.print(s);
    }
  }

  @Override
  public void print(Object obj) {
    for (PrintStream stream : delegates) {
      stream.print(obj);
    }
  }

  @Override
  public void println() {
    for (PrintStream stream : delegates) {
      stream.println();
    }
  }

  @Override
  public void println(boolean x) {
    for (PrintStream stream : delegates) {
      stream.println(x);
    }
  }

  @Override
  public void println(char x) {
    for (PrintStream stream : delegates) {
      stream.println(x);
    }
  }

  @Override
  public void println(int x) {
    for (PrintStream stream : delegates) {
      stream.println(x);
    }
  }

  @Override
  public void println(long x) {
    for (PrintStream stream : delegates) {
      stream.println(x);
    }
  }

  @Override
  public void println(float x) {
    for (PrintStream stream : delegates) {
      stream.println(x);
    }
  }

  @Override
  public void println(double x) {
    for (PrintStream stream : delegates) {
      stream.println(x);
    }
  }

  @Override
  public void println(char[] x) {
    for (PrintStream stream : delegates) {
      stream.println(x);
    }
  }

  @Override
  public void println(String x) {
    for (PrintStream stream : delegates) {
      stream.println(x);
    }
  }

  @Override
  public void println(Object x) {
    for (PrintStream stream : delegates) {
      stream.println(x);
    }
  }

  @Override
  public PrintStream printf(String format, Object... args) {
    for (PrintStream stream : delegates) {
      stream.printf(format, args);
    }
    return this;
  }

  @Override
  public PrintStream printf(Locale l, String format, Object... args) {
    for (PrintStream stream : delegates) {
      stream.printf(l, format, args);
    }
    return this;
  }

  @Override
  public PrintStream format(String format, Object... args) {
    for (PrintStream stream : delegates) {
      stream.printf(format, args);
    }
    return this;
  }

  @Override
  public PrintStream format(Locale l, String format, Object... args) {
    for (PrintStream stream : delegates) {
      stream.printf(format, args);
    }
    return this;
  }

  @Override
  public PrintStream append(CharSequence csq) {
    for (PrintStream stream : delegates) {
      stream.append(csq);
    }
    return this;
  }

  @Override
  public PrintStream append(CharSequence csq, int start, int end) {
    for (PrintStream stream : delegates) {
      stream.append(csq, start, end);
    }
    return this;
  }

  @Override
  public PrintStream append(char c) {
    for (PrintStream stream : delegates) {
      stream.append(c);
    }
    return this;
  }

  @Override
  public void write(byte[] b) throws IOException {
    for (PrintStream stream : delegates) {
      stream.write(b);
    }
  }
}
