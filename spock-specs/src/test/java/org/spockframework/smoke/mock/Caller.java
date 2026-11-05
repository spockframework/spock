package org.spockframework.smoke.mock;

import java.util.concurrent.Callable;

public interface Caller {
  Throwable call(Callable callable) throws Exception;
}
