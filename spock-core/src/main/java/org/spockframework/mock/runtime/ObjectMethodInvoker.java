package org.spockframework.mock.runtime;

import org.spockframework.mock.*;
import org.spockframework.util.ThreadSafe;

import java.util.function.Supplier;

@ThreadSafe
public class ObjectMethodInvoker implements IResponseGenerator {

  public static final ObjectMethodInvoker INSTANCE = new ObjectMethodInvoker();

  private ObjectMethodInvoker() {
  }

  @Override
  public Supplier<Object> respond(IMockInvocation invocation) {
    return () -> {
      IMockInteraction interaction = DefaultJavaLangObjectInteractions.INSTANCE.match(invocation);
      if (interaction != null) {
        return interaction.accept(invocation).get();
      }

      return null;
    };
  }
}
