package org.spockframework.mock;

import org.spockframework.util.Nullable;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents an invoked method on a mock object. In the case of a dynamic method
 * call, this may not necessarily correspond to a physical method.
 */
public interface IMockMethod {
  String getName();

  /**
   * The parameter types of the invoked method. If not a physical method, the types of
   * the actual arguments are returned.
   */
  List<Class<?>> getParameterTypes();

  /**
   * The return type of the invoked method. If not a physical method, Object.class is returned.
   */
  Class<?> getReturnType();

  @Nullable
  Method getTargetMethod();
}
