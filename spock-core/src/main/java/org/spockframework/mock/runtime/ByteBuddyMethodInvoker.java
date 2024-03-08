package org.spockframework.mock.runtime;

import org.spockframework.mock.*;
import org.spockframework.util.ExceptionUtil;
import org.spockframework.util.ThreadSafe;

import java.util.function.Supplier;

@ThreadSafe
public class ByteBuddyMethodInvoker implements IResponseGenerator {

  private final ByteBuddyInvoker superCall;

  public ByteBuddyMethodInvoker(ByteBuddyInvoker superCall) {
    this.superCall = superCall;
  }

  @Override
  public Supplier<Object> respond(IMockInvocation invocation) {
    return () -> {
      if (superCall == null) {
        throw new IllegalStateException("Cannot invoke abstract method " + invocation.getMethod());
      }
      try {
        return superCall.call(invocation.getArguments().toArray());
      } catch (Throwable t) {
        // Byte Buddy doesn't wrap exceptions in InvocationTargetException, so no need to unwrap
        return ExceptionUtil.sneakyThrow(t);
      }
    };
  }
}
