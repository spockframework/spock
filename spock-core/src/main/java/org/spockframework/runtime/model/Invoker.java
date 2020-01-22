package org.spockframework.runtime.model;


@FunctionalInterface
public interface Invoker {
  Object invoke(Object target, Object... arguments) throws Throwable;
}
