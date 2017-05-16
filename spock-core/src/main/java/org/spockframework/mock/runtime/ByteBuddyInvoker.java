package org.spockframework.mock.runtime;

public interface ByteBuddyInvoker {

  Object call(Object[] arguments);
}
