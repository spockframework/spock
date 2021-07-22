package org.spockframework.mock;

import org.spockframework.util.*;
import spock.mock.MockingApi;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Configuration options for mock objects. Once a mock object has been created, its configuration cannot be changed.
 *
 * <p>{@link #getNature()} and {@link #getImplementation()} are mandatory options that are typically
 * determined by choosing the appropriate {@link MockingApi} factory method. {@link #getType()}
 * is a mandatory option that is typically passed directly to a {@code MockingApi} factory method, or inferred
 * from the left-hand side of the enclosing variable assignment. The remaining options are optional and are
 * typically passed to a {@code MockingApi} factory method as named arguments. For example,
 * {@link #getConstructorArgs()} corresponds to the {@code constructorArgs:} named argument,
 * {@link #isGlobal()} to the {@code global:} named argument, etc.
 */
@Beta
public interface IMockConfiguration {
  /**
   * Returns the name of the mock object.
   *
   * @return the name of the mock object
   */
  @Nullable
  String getName();

  /**
   * Returns the interface or class type of the mock object.
   *
   * @return the interface or class type of the mock object
   */
  Class<?> getType();

  /**
   * Returns the instance to be used as a prototype
   *
   * @return the instance to be used as a prototype
   */
  @Nullable
  Object getInstance();

  /**
   * Returns the exact interface or class type of the mock object.
   * The returned {@link Type} is either a {@link Class} or a {@link java.lang.reflect.ParameterizedType}.
   *
   * @return the exact interface or class type of the mock object
   */
  Type getExactType();

  /**
   * Returns the nature of the mock object. A nature is a named
   * set of defaults for mock configuration options.
   *
   * @return the nature of the mock object
   */
  MockNature getNature();

  /**
   * Returns the implementation of the mock object.
   *
   * @return the implementation of the mock object
   */
  MockImplementation getImplementation();

  /**
   * Returns the constructor arguments to be used for creating the mock object.
   *
   * @return the constructor arguments to be used for creating the mock object
   */
  @Nullable
  List<Object> getConstructorArgs();

  /**
   * Returns the list of additional interfaces to be implemented by the mock.
   *
   * @since 1.2
   * @return the list of additional interfaces to be implemented by the mock
   */
  List<Class<?>> getAdditionalInterfaces();

  /**
   * Returns the default response strategy for the mock object.
   *
   * @return the default response strategy for the mock object
   */
  IDefaultResponse getDefaultResponse();

  /**
   * Tells whether this mock object supports last defined return value.
   * By default Spock uses a match first algorithm to determine the defined return value of a method.
   *
   * @return whether this mock object supports last matched response
   */
  boolean useLastMatchResponseStrategy();

  /**
   * Tells whether a mock object stands in for all objects of the mocked type, or just for itself.
   * This is an optional feature that may not be supported by a particular {@link MockImplementation}.
   *
   * @return whether a mock object stands in for all objects of the mocked type, or just for itself
   */
  boolean isGlobal();

  /**
   * Tells whether invocations on the mock object should be verified. If (@code false}, invocations
   * on the mock object will not be matched against interactions that have a cardinality.
   *
   * @return whether invocations on the mock object should be verified
   */
  boolean isVerified();

  /**
   * Tells whether the Objenesis library, if available on the class path, should be used for constructing
   * the mock object, rather than calling a constructor.
   *
   * @return whether the Objenesis library should be used for constructing the mock object
   */
  boolean isUseObjenesis();
}
