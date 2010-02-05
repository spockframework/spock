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

import org.junit.runner.RunWith;

import org.spockframework.mock.MockController;
import org.spockframework.runtime.*;

import groovy.lang.Closure;

/**
 * Base class for Spock specifications. All specifications must inherit from
 * this class, either directly or indirectly.
 * 
 * @author Peter Niederwieser
 */
// NOTE: if method implementations are declared private instead of package private,
// they are no longer visible to Specs that extend spock.lang.Specification
// (runtime dispatch fails)
@RunWith(Sputnik.class)
public abstract class Specification {
  /**
   * The wildcard symbol. Used in several places as a "don't care" value.
   */
  public static final Object _ = new Wildcard();

  /**
   * Specifies that the preceding <tt>when</tt> block should throw an exception.
   * May only occur as the initializer expression of a typed variable declaration
   * in a <tt>then</tt> block; the expected exception type is inferred from the
   * variable type.
   * <p>This form of exception condition is typically used if the thrown
   * exception instance is used in subsequent conditions.
   *
   * <p>Example:
   * <pre>
   * when:
   * "".charAt(0)
   *
   * then:
   * IndexOutOfBoundsException e = thrown()
   * e.message.contains(...)
   * </pre>
   *
   * @return the thrown exception instance
   */
  public Throwable thrown() {
    throw new InvalidSpecException(
        "Exception conditions are only allowed in 'then' blocks, and may not be nested inside other elements");
  }

  /**
   * Specifies that the preceding <tt>when</tt> block should throw an exception
   * of the given type. May only occur in a <tt>then</tt> block.
   * <p>This form of exception condition is typically used if the thrown
   * exception instance is <em>not</em> used in subsequent conditions.
   *
   * <p>Example:
   * <pre>
   * when:
   * "".charAt(0)
   *
   * then:
   * thrown(IndexOutOfBoundsException)
   *
   * @param type the expected exception type
   * @param <T> the expected exception type
   * @return the thrown exception instance
   */
  @SuppressWarnings("UnusedDeclaration")
  public <T extends Throwable> T thrown(Class<T> type) {
    throw new InvalidSpecException(
        "Exception conditions are only allowed in 'then' blocks, and may not be nested inside other elements");
  }

  /**
   * Specifies that in particular, no exception of the given type should be
   * thrown. This method has only documentation purposes and does not affect
   * the execution of the specification.
   *
   * @param type an exception type
   */
  @SuppressWarnings("UnusedDeclaration")
  public void notThrown(Class<? extends Throwable> type) {
    // IDEA: provide an implementation that makes it possible to differentiate
    // between this exception being thrown, and any other exception being thrown
  }

  /**
   * Specifies that no exception should be thrown. Equivalent to
   * <tt>notThrown(Throwable)</tt>. This method has only documentation purposes
   * and does not affect the execution of the specification.
   */
  @SuppressWarnings("UnusedDeclaration")
  public void noExceptionThrown() { /* nothing to do */ }

  /**
   * Creates a mock object whose name and type are inferred from the variable
   * that the mock object is assigned to. For example,
   * <tt>IOrderService service = Mock()</tt> will create a mock object named
   * "service" and of type <tt>IOrderService</tt>.
   *
   * @return the new mock object
   */
  public Object Mock() {
    throw new InvalidSpecException("Mock objects may only be created during the lifetime of a feature (iteration)");
  }

  /**
   * Creates a mock object of the given type. If this method is used
   * to initialize a new variable, the mock's name is inferred from the
   * variable's name. For example, <tt>def service = Mock(IOrderService)</tt>
   * will create a mock object named "service" and of type
   * <tt>IOrderService</tt>. Otherwise, the mock will be named after
   * its type (e.g. "IOrderService").
   *
   * @param type the type of the mock object to be created
   * @param <T> the type of the mock object to be created
   * @return the new mock object
   */
  @SuppressWarnings("UnusedDeclaration")
  public <T> T Mock(Class<T> type) {
    throw new InvalidSpecException("Mock objects can only be created inside a Spec");
  }

  /**
   * Encloses one or more interaction definitions in a <tt>then</tt> block.
   * Required when an interaction definition uses a statement that doesn't
   * match one of the following patterns, and therefore isn't automatically
   * recognized as belonging to an interaction definition:
   * <ul>
   * <li><tt>num * target.method(args)</tt></li>
   * <li><tt>target.method(args) >>(>) result(s)</tt></li>
   * <li><tt>num * target.method(args) >>(>) result(s)</li>
   * </ul>
   *
   * <p>Regular interaction definition:
   * <pre>
   * def "published messages are received at least once"() {
   *   when:
   *   publisher.send(msg)
   *
   *   then:
   *   (1.._) * subscriber.receive(msg)
   * }
   * </pre>
   *
   * <p>Equivalent definition that uses a helper variable:
   * <pre>
   * def "published messages are received at least once"() {
   *   when:
   *   publisher.send(msg)
   *
   *   then:
   *   interaction {
   *     def num = (1.._)
   *     num * subscriber.receive(msg)
   *   }
   * }
   * </pre>
   *
   * <p>Equivalent definition that uses a helper method:
   * <pre>
   * def "published messages are received at least once"() {
   *   when:
   *   publisher.send(msg)
   *
   *   then:
   *   interaction {
   *     messageReceived(msg)
   *   }
   * }
   *
   * def messageReceived(msg) {
   *   (1.._) * subscriber.receive(msg)
   * }
   * </pre>
   *
   * @param block a block of code containing one or more interaction definitions
   */
  public void interaction(Closure block) {
    block.call();
  }

  /**
   * Used in a then-block to access an expression's value at the time just
   * before the previous where-block was entered.
   *
   * @param expression an arbitrary expression, except that it may not
   * reference variables defined in the then-block
   * @param <T> the expression's type
   * @return the expression's value at the time the previous where-block was
   * entered
   */
  @SuppressWarnings("UnusedDeclaration")
  public <T> T old(T expression) {
    throw new InvalidSpecException("old() can only be used in a 'then' block");
  }

  @SuppressWarnings("UnusedDeclaration")
  <T extends Throwable> T thrown(Class<T> type, String name, Throwable exception) {
    if (type.isInstance(exception)) return type.cast(exception);
    throw new WrongExceptionThrownError(type, exception);
  }

  @SuppressWarnings("UnusedDeclaration")
  <T> T Mock(Class<T> type, String name, MockController controller) {
    if (type == null)
      throw new InvalidSpecException("Mock object type may not be 'null'");

    if (controller == null) {
      // mock has been created in a context where no controller exists
      Mock();
      return null; // unreachable; just exists to avoid compiler warning
    }

    if (name == null) name = type.getSimpleName();

    return type.cast(controller.create(name, type));
  }

  // dummy parameter exists just to create a new overload of old() with different implementation
  @SuppressWarnings("UnusedDeclaration")
  <T> T old(T expression, boolean dummy) {
    return expression;
  }

  // non-anonymous class to facilitate debugging
  private static class Wildcard {
    @Override
    public String toString() {
      return "_"; // both compiler and runtime sometimes need String representation
    }
  }
}
