package org.spockframework.mock.runtime;

import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.IMockMethod;
import org.spockframework.mock.IResponseGenerator;
import org.spockframework.util.ExceptionUtil;
import org.spockframework.util.ReflectionUtil;

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
      Object userCreatedInstance = invocation.getMockObject().getUserCreatedInstance();
      if(userCreatedInstance != null) {
        IMockMethod method = invocation.getMethod();
        Class<?>[] parameterTypes = method.getParameterTypes().toArray(new Class<?>[method.getParameterTypes().size()]);
        return  ReflectionUtil.getMethodBySignature(userCreatedInstance.getClass(), method.getName(), parameterTypes).invoke(userCreatedInstance, invocation.getArguments().toArray());
      }
      return superCall.call(invocation.getArguments().toArray());
    } catch (Throwable t) {
      // Byte Buddy doesn't wrap exceptions in InvocationTargetException, so no need to unwrap
      ExceptionUtil.sneakyThrow(t);
      return null; // unreachable
    }
  }
}
