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

import org.junit.runner.RunWith;
import org.spockframework.util.Beta;
import org.spockframework.lang.Wildcard;
import org.spockframework.util.ExceptionUtil;
import org.spockframework.runtime.*;
import org.spockframework.runtime.GroovyRuntimeUtil;
import spock.mock.MockingApi;

/**
 * Base class for Spock specifications. All specifications must inherit from
 * this class, either directly or indirectly.
 *
 * @author Peter Niederwieser
 */
@RunWith(Sputnik.class)
@SuppressWarnings("UnusedDeclaration")
public abstract class Specification extends MockingApi {
  /**
   * The wildcard symbol. Used in several places as a <em>don't care</em> value:
   * <ul>
   * <li>Mock interactions</li>
   * Example: <tt>1 * foo.bar(_)</tt>
   * <li>Data parameterizations</li>
   * Example: <tt>[foo, _] &lt;&lt; loadDataFromDb()</tt>
   * </ul>
   */
  public static final Object _ = Wildcard.INSTANCE;

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
  public <T extends Throwable> T thrown() {
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
  public <T extends Throwable> T thrown(Class<T> type) {
    throw new InvalidSpecException(
        "Exception conditions are only allowed in 'then' blocks, and may not be nested inside other elements");
  }

  /**
   * Specifies that no exception of the given type should be
   * thrown, failing with a {@link UnallowedExceptionThrownError} otherwise.
   *
   * @param type the exception type that should not be thrown
   */
  public void notThrown(Class<? extends Throwable> type) {
    Throwable thrown = getSpecificationContext().getThrownException();
    if (thrown == null) return;
    if (type.isAssignableFrom(thrown.getClass())) {
      throw new UnallowedExceptionThrownError(type, thrown);
    }
    ExceptionUtil.sneakyThrow(thrown);
  }

  /**
   * Specifies that no exception should be thrown, failing with a
   * {@link UnallowedExceptionThrownError} otherwise.
   */
  public void noExceptionThrown() {
    Throwable thrown = getSpecificationContext().getThrownException();
    if (thrown == null) return;
    throw new UnallowedExceptionThrownError(null, thrown);
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
  public <T> T old(T expression) {
    throw new InvalidSpecException("old() can only be used in a 'then' block");
  }

  /**
   * Sets the specified object as the implicit target of the top-level conditions and/or
   * interactions contained in the specified code block, thereby avoiding the need to repeat
   * the same expression multiple times. Implicit conditions are supported. (In other words,
   * the {@code assert} keyword may be omitted.) If the target is {@code null}, a
   * {@code SpockAssertionError} is thrown.
   *
   * <p>A {@code with} block can be used anywhere in a spec, including nested positions
   * and helper methods.
   *
   * <p>Condition example:
   *
   * <pre>
   * def fred = new Person(name: "Fred", age: 42)
   * def spaceship = new Spaceship(pilot: fred)
   *
   * expect:
   * with(spaceship.pilot) {
   *   name == "Fred" // shorthand for: spaceship.pilot.name == "Fred"
   *   age == 42
   * }
   * </pre>
   *
   * <p> Interaction example:
   *
   * <pre>
   * def service = Mock(Service) // has start(), stop(), and doWork() methods
   * def app = new Application(service) // controls the lifecycle of the service
   *
   * when:
   * app.run()
   *
   * then:
   * with(service) {
   *   1 * start() // shorthand for: 1 * service.start()
   *   1 * doWork()
   *   1 * stop()
   * }
   * </pre>
   *
   * @param target an implicit target for conditions and/or interactions
   * @param closure a code block containing top-level conditions and/or interactions
   */
  @Beta
  public void with(Object target, Closure<?> closure) {
    if (target == null) {
      throw new SpockAssertionError("Target of 'with' block must not be null");
    }
    closure.setDelegate(target); // for conditions
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    GroovyRuntimeUtil.invokeClosure(closure, target);
  }

  /**
   * Same as {@link #with(Object, groovy.lang.Closure)}, except that it also states that
   * the specified target has the specified type, throwing a {@code SpockAssertionError}
   * otherwise. As a side effect, this may give better code completion in IDEs.
   *
   * <p>Example:
   *
   * <pre>
   * def fred = new Employee(name: "Fred", age: 42, employer: "MarsTravelUnited")
   * def spaceship = new Spaceship(pilot: fred)
   *
   * expect:
   * with(spaceship.pilot, Employee) {
   *   name == "Fred" // shorthand for: spaceship.pilot.name == "Fred"
   *   age == 42
   *   employer == "MarsTravelUnited"
   * }
   * </pre>
   *
   * @param target an implicit target for conditions and/or interactions
   * @param type the expected type of the target
   * @param closure a code block containing top-level conditions and/or interactions
   */
  @Beta
  public void with(Object target, Class<?> type, Closure closure) {
    if (target != null && !type.isInstance(target)) {
      throw new SpockAssertionError(String.format("Expected target of 'with' block to have type '%s', but got '%s'",
          type, target.getClass().getName()));
    }
    with(target, closure);
  }

  public void verifyAll(Closure closure){
    GroovyRuntimeUtil.invokeClosure(closure);
  }
}
