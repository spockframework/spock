/*
 * Copyright 2013 the original author or authors.
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

package org.spockframework.util;

import java.io.*;

/**
 * Delegates all PrintStream invocations to {@link #printed(String)}.
 * Aims to keep the original invocation granularity.
 */
public abstract class StringMessagePrintStream extends PrintStream {
  public StringMessagePrintStream() {
    super(new ByteArrayOutputStream(0));
  }

  @Override
  public void write(int b) {
    InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(new byte[] {(byte) b}));
    try {
      printed(String.valueOf((char) reader.read()));
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
      printed(String.valueOf(target, 0, actualLen));
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
    printed(String.valueOf(b));
  }

  @Override
  public void print(char c) {
    printed(String.valueOf(c));
  }

  @Override
  public void print(int i) {
    printed(String.valueOf(i));
  }

  @Override
  public void print(long l) {
    printed(String.valueOf(l));
  }

  @Override
  public void print(float f) {
    printed(String.valueOf(f));
  }

  @Override
  public void print(double d) {
    printed(String.valueOf(d));
  }

  @Override
  public void print(char[] s) {
    printed(String.valueOf(s));
  }

  @Override
  public void print(String s) {
    printed(String.valueOf(s));
  }

  @Override
  public void print(Object obj) {
    printed(String.valueOf(obj));
  }

  // we want a single notification for println() methods,
  // so we don't delegate to print() methods

  @Override
  public void println() {
    printed("\n");
  }

  @Override
  public void println(boolean x) {
    printed(String.valueOf(x) + "\n");
  }

  @Override
  public void println(char x) {
    printed(String.valueOf(x) + "\n");
  }

  @Override
  public void println(int x) {
    printed(String.valueOf(x) + "\n");
  }

  @Override
  public void println(long x) {
    printed(String.valueOf(x) + "\n");
  }

  @Override
  public void println(float x) {
    printed(String.valueOf(x) + "\n");
  }

  @Override
  public void println(double x) {
    printed(String.valueOf(x) + "\n");
  }

  @Override
  public void println(char[] x) {
    printed(String.valueOf(x) + "\n");
  }

  @Override
  public void println(String x) {
    printed(String.valueOf(x) + "\n");
  }

  @Override
  public void println(Object x) {
    printed(String.valueOf(x) + "\n");
  }

  @Override
  public PrintStream append(CharSequence csq) {
    printed(String.valueOf(csq));
    return this;
  }

  @Override
  public PrintStream append(CharSequence csq, int start, int end) {
    CharSequence seq = csq == null ? "null" : csq;
    printed(seq.subSequence(start, end).toString());
    return this;
  }

  @Override
  public PrintStream append(char c) {
    print(c);
    return this;
  }

  // we are relying on printf() and format() methods to eventually delegate to append()
  // TODO: printf() and format() methods should produce a single printed() event

  protected abstract void printed(String message);
}
