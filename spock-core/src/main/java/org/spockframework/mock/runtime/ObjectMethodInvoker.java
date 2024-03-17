package org.spockframework.mock.runtime;

import org.spockframework.mock.*;
import org.spockframework.util.ThreadSafe;

@ThreadSafe
public class ObjectMethodInvoker implements IResponseGenerator {

  public static final ObjectMethodInvoker INSTANCE = new ObjectMethodInvoker();

  private ObjectMethodInvoker() {
  }

  @Override
  public Object respond(IMockInvocation invocation) {
    IMockInteraction interaction = DefaultJavaLangObjectInteractions.INSTANCE.match(invocation);
    if (interaction != null) {
      return interaction.accept(invocation).get();
    }

    return null;
  }
}
