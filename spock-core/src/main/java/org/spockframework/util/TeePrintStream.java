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
  private final PrintStream original;
  private final PrintStream delegate;

  private volatile boolean includeOriginal = true;

  public TeePrintStream(PrintStream original, PrintStream delegate) {
    super(new ByteArrayOutputStream(0));
    this.original = original;
    this.delegate = delegate;
  }

  @Deprecated
  public TeePrintStream(PrintStream... delegates) {
    this(asList(delegates));
  }

  @Deprecated
  public TeePrintStream(List<PrintStream> delegates) {
    this(delegates.get(0), delegates.get(1));
    Checks.checkArgument(delegates.size() == 2, () -> "Only accepts two arguments");
  }
  @Deprecated
  public List<PrintStream> getDelegates() {
    return asList(original, delegate);
  }

  public PrintStream getOriginal() {
    return original;
  }

  public PrintStream getDelegate() {
    return delegate;
  }

  public void muteOriginal() {
    includeOriginal = false;
  }

  public void unmuteOriginal() {
    includeOriginal = true;
  }

  @Deprecated
  public void stopDelegation() {

  }

  @Override
  public void flush() {
    if (includeOriginal) {
      original.flush();
    }
    delegate.flush();
  }

  @Override
  public void close() {
    original.close();
    delegate.close();
  }

  @Override
  public boolean checkError() {
    return original.checkError() || delegate.checkError();
  }

  @Override
  protected void setError() {
    throw new UnsupportedOperationException("setError");
  }

  @Override
  protected void clearError() {
    for (PrintStream stream : asList(original, delegate)) {
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
    if (includeOriginal) {
      original.write(b);
    }
    delegate.write(b);
  }

  @Override
  public void write(byte[] buf, int off, int len) {
    if (includeOriginal) {
      original.write(buf, off, len);
    }
    delegate.write(buf, off, len);
  }

  @Override
  public void print(boolean b) {
    if (includeOriginal) {
      original.print(b);
    }
    delegate.print(b);
  }

  @Override
  public void print(char c) {
    if (includeOriginal) {
      original.print(c);
    }
    delegate.print(c);
  }

  @Override
  public void print(int i) {
    if (includeOriginal) {
      original.print(i);
    }
    delegate.print(i);
  }

  @Override
  public void print(long l) {
    if (includeOriginal) {
      original.print(l);
    }
    delegate.print(l);
  }

  @Override
  public void print(float f) {
    if (includeOriginal) {
      original.print(f);
    }
    delegate.print(f);
  }

  @Override
  public void print(double d) {
    if (includeOriginal) {
      original.print(d);
    }
    delegate.print(d);
  }

  @Override
  public void print(char[] s) {
    if (includeOriginal) {
      original.print(s);
    }
    delegate.print(s);
  }

  @Override
  public void print(String s) {
    if (includeOriginal) {
      original.print(s);
    }
    delegate.print(s);
  }

  @Override
  public void print(Object obj) {
    if (includeOriginal) {
      original.print(obj);
    }
    delegate.print(obj);
  }

  @Override
  public void println() {
    if (includeOriginal) {
      original.println();
    }
    delegate.println();
  }

  @Override
  public void println(boolean x) {
    if (includeOriginal) {
      original.println(x);
    }
    delegate.println(x);
  }

  @Override
  public void println(char x) {
    if (includeOriginal) {
      original.println(x);
    }
    delegate.println(x);
  }

  @Override
  public void println(int x) {
    if (includeOriginal) {
      original.println(x);
    }
    delegate.println(x);
  }

  @Override
  public void println(long x) {
    if (includeOriginal) {
      original.println(x);
    }
    delegate.println(x);
  }

  @Override
  public void println(float x) {
    if (includeOriginal) {
      original.println(x);
    }
    delegate.println(x);
  }

  @Override
  public void println(double x) {
    if (includeOriginal) {
      original.println(x);
    }
    delegate.println(x);
  }

  @Override
  public void println(char[] x) {
    if (includeOriginal) {
      original.println(x);
    }
    delegate.println(x);
  }

  @Override
  public void println(String x) {
    if (includeOriginal) {
      original.println(x);
    }
    delegate.println(x);
  }

  @Override
  public void println(Object x) {
    if (includeOriginal) {
      original.println(x);
    }
    delegate.println(x);
  }

  @Override
  public PrintStream printf(String format, Object... args) {
    if (includeOriginal) {
      original.printf(format, args);
    }
    delegate.printf(format, args);
    return this;
  }

  @Override
  public PrintStream printf(Locale l, String format, Object... args) {
    if (includeOriginal) {
      original.printf(l, format, args);
    }
    delegate.printf(l, format, args);
    return this;
  }

  @Override
  public PrintStream format(String format, Object... args) {
    if (includeOriginal) {
      original.printf(format, args);
    }
    delegate.printf(format, args);
    return this;
  }

  @Override
  public PrintStream format(Locale l, String format, Object... args) {
    if (includeOriginal) {
      original.printf(format, args);
    }
    delegate.printf(format, args);
    return this;
  }

  @Override
  public PrintStream append(CharSequence csq) {
    if (includeOriginal) {
      original.append(csq);
    }
    delegate.append(csq);
    return this;
  }

  @Override
  public PrintStream append(CharSequence csq, int start, int end) {
    if (includeOriginal) {
      original.append(csq, start, end);
    }
    delegate.append(csq, start, end);
    return this;
  }

  @Override
  public PrintStream append(char c) {
    if (includeOriginal) {
      original.append(c);
    }
    delegate.append(c);
    return this;
  }

  @Override
  public void write(byte[] b) throws IOException {
    if (includeOriginal) {
      original.write(b);
    }
    delegate.write(b);
  }
}
