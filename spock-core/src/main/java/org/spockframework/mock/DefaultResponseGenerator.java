package org.spockframework.mock;

public class DefaultResponseGenerator implements IResponseGenerator {
  public Object respond(IMockInvocation invocation) {
    return invocation.getMockObject().getDefaultResponse().respond(invocation);
  }
}
