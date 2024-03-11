package org.spockframework.mock.runtime;

import org.spockframework.mock.*;
import org.spockframework.util.ExceptionUtil;
import org.spockframework.util.ThreadSafe;

@ThreadSafe
public class ByteBuddyMethodInvoker implements IResponseGenerator {

  private final ByteBuddyInvoker superCall;

  public ByteBuddyMethodInvoker(ByteBuddyInvoker superCall) {
    this.superCall = superCall;
  }

  @Override
  public Object respond(IMockInvocation invocation) {
    if (superCall == null) {
      throw new IllegalStateException("Cannot invoke abstract method " + invocation.getMethod());
    }
    try {
      return superCall.call(invocation.getArguments().toArray());
    } catch (Throwable t) {
      // Byte Buddy doesn't wrap exceptions in InvocationTargetException, so no need to unwrap
      return ExceptionUtil.sneakyThrow(t);
    }
  }
}
