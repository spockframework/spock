/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.lang;

import groovy.lang.Closure;

import org.spockframework.mock.MockController;
import org.spockframework.runtime.SpeckAssertionError;
import org.spockframework.util.SyntaxException;

/**
 * Predefined properties and methods that are automatically imported for every Speck.
 *
 * @author Peter Niederwieser
 */
// IDEA: move method implementations to SpockRuntime to avoid confusion for user
public class Predef {
  public static final Object _ = new PlaceHolder();

  public static Throwable thrown() {
    throw new SyntaxException(
        "Exception conditions are only allowed in 'then' blocks, and may not be nested inside other elements");
  }

  @SuppressWarnings("UnusedDeclaration")
  public static <T extends Throwable> T thrown(Class<T> type) {
    throw new SyntaxException(
        "Exception conditions are only allowed in 'then' blocks, and may not be nested inside other elements");
  }

  @SuppressWarnings("UnusedDeclaration")
  public static boolean notThrown(Class<? extends Throwable> type) {
    // TODO: provide an implementation that makes it easy to differentiate
    // between this exception being thrown, and any other exception being thrown
    // if this needs special compiler handling, return type can be changed back
    // to void
    return true;
  }

  /**
   * Creates a mock object whose name and type are inferred from the variable
   * that the mock object is assigned to. For example,
   * <tt>IOrderService service = mock()</tt> will create a mock object named
   * "service" of type <tt>IOrderService</tt>.
   *
   * @return a newly created mock object
   */
  public static Object Mock() {
    throw new SyntaxException("Mock objects can only be created inside a Speck");
  }

  /**
   * Creates a mock object of the given type.
   *
   * @param type the type of the mock object to be created
   * @param <T> the type of the mock object to be created
   * @return a newly created mock object
   */
  @SuppressWarnings("UnusedDeclaration")
  public static <T> T Mock(Class<T> type) {
    throw new SyntaxException("Mock objects can only be created inside a Speck");
  }

  public static void interaction(Closure interactions) {
    interactions.call();
  }

  @SuppressWarnings("UnusedDeclaration")
  private static <T extends Throwable> T thrown(Class<T> type, String name, Throwable exception) {
    if (type.isInstance(exception)) return type.cast(exception);
    throw new SpeckAssertionError("Expected exception %s, but %s", type.getName(),
        exception == null ? "no exception was thrown" : ("got: " + exception));
  }

  @SuppressWarnings("UnusedDeclaration")
  private static <T> T Mock(Class<T> type, String name, MockController controller) {
    if (type == null)
      throw new SyntaxException("Mock object type may not be 'null'");
    return type.cast(controller.create(name, type));
  }

  // named rather than anonymous class to facilitate debugging
  private static class PlaceHolder {
    @Override
    public String toString() {
      return "_"; // both compiler and runtime sometimes need String representation
    }
  }
}
