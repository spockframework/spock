package org.spockframework.smoke.mock;

import java.util.concurrent.Callable;

public class JavaCaller implements Caller {
  @Override
  public Throwable call(Callable callable) throws Exception {
    try {
      callable.call();
      return null;
    } catch (Throwable t) {
      return t;
    }
  }
}
