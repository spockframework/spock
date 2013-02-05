/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.util;

import java.io.*;

public abstract class StandardStreamNotifier extends PrintStream {
  protected final IStandardStreamListener listener;

  public StandardStreamNotifier(IStandardStreamListener listener) {
    super(new ByteArrayOutputStream(0));
    this.listener = listener;
  }

  @Override
  public void write(int b) {
    InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(new byte[] {(byte) b}));
    try {
      notify(String.valueOf((char) reader.read()));
    } catch (IOException e) {
      throw new InternalSpockError(e);
    }
  }

  @Override
  public void write(byte[] buf, int off, int len) {
    byte[] source = new byte[len];
    System.arraycopy(buf, off, source, 0, len);
    InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(source));
    try {
      char[] target = new char[len];
      int actualLen = reader.read(target, 0, len);
      notify(String.valueOf(target, 0, actualLen));
    } catch (IOException e) {
      throw new InternalSpockError(e);
    }
  }

  @Override
  public void write(byte[] b) throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void print(boolean b) {
    notify(String.valueOf(b));
  }

  @Override
  public void print(char c) {
    notify(String.valueOf(c));
  }

  @Override
  public void print(int i) {
    notify(String.valueOf(i));
  }

  @Override
  public void print(long l) {
    notify(String.valueOf(l));
  }

  @Override
  public void print(float f) {
    notify(String.valueOf(f));
  }

  @Override
  public void print(double d) {
    notify(String.valueOf(d));
  }

  @Override
  public void print(char[] s) {
    notify(String.valueOf(s));
  }

  @Override
  public void print(String s) {
    notify(String.valueOf(s));
  }

  @Override
  public void print(Object obj) {
    notify(String.valueOf(obj));
  }

  // we want a single notification for println() methods,
  // so we don't delegate to print() methods

  @Override
  public void println() {
    notify("\n");
  }

  @Override
  public void println(boolean x) {
    notify(String.valueOf(x) + "\n");
  }

  @Override
  public void println(char x) {
    notify(String.valueOf(x) + "\n");
  }

  @Override
  public void println(int x) {
    notify(String.valueOf(x) + "\n");
  }

  @Override
  public void println(long x) {
    notify(String.valueOf(x) + "\n");
  }

  @Override
  public void println(float x) {
    notify(String.valueOf(x) + "\n");
  }

  @Override
  public void println(double x) {
    notify(String.valueOf(x) + "\n");
  }

  @Override
  public void println(char[] x) {
    notify(String.valueOf(x) + "\n");
  }

  @Override
  public void println(String x) {
    notify(String.valueOf(x) + "\n");
  }

  @Override
  public void println(Object x) {
    notify(String.valueOf(x) + "\n");
  }

  @Override
  public PrintStream append(CharSequence csq) {
    notify(String.valueOf(csq));
    return this;
  }

  @Override
  public PrintStream append(CharSequence csq, int start, int end) {
    CharSequence seq = csq == null ? "null" : csq;
    notify(seq.subSequence(start, end).toString());
    return this;
  }

  @Override
  public PrintStream append(char c) {
    print(c);
    return this;
  }

  // we are relying on printf() and format() methods to eventually delegate to append()
  // TODO: printf() and format() methods should produce a single notify() event

  protected abstract void notify(String string);
}
