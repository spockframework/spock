package org.spockframework.mock;

import org.spockframework.util.Nullable;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents a method that is to be mocked. May or may not correspond to a physical method in byte code.
 */
public interface IMockMethod {
  /**
   * The name of the mocked method.
   *
   * @return name of the mocked method
   */
  String getName();

  /**
   * The parameter types of the mocked method. In cases where no static type information is available,
   * all arguments are assumed to have type {@code Object}.
   *
   * @return the parameter types of the mocked method
   */
  List<Class<?>> getParameterTypes();

  /**
   * The return type of the mocked method. In cases where no static type information is available,
   * the return type is assumed to be {@code Object}.
   *
   * @return the return type of the mocked method
   */
  Class<?> getReturnType();

  /**
   * Returns the physical method corresponding to the mocked method. Returns {@code null} if a dynamic
   * method is mocked, or if the target method isn't known because the invocation is intercepted before
   * a method has been selected.
   *
   * @return the physical method corresponding to the mocked method
   */
  @Nullable
  Method getTargetMethod();
}
