package org.spockframework.mock.response;

import org.spockframework.mock.IMockInvocation;
import org.spockframework.mock.IResponseGenerator;

public class DefaultResponseGenerator implements IResponseGenerator {
  public Object respond(IMockInvocation invocation) {
    return invocation.getMockObject().getDefaultResponse().respond(invocation);
  }
}
