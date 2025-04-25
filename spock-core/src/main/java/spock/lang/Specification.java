/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package spock.lang;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.*;
import org.junit.platform.commons.annotation.Testable;
import org.spockframework.lang.ISpecificationContext;
import org.spockframework.lang.Wildcard;
import org.spockframework.runtime.*;
import org.spockframework.util.Beta;
import org.spockframework.util.ExceptionUtil;
import spock.mock.MockingApi;

import java.util.Objects;
import java.util.function.Function;

/**
 * Base class for Spock specifications. All specifications must inherit from
 * this class, either directly or indirectly.
 *
 * @author Peter Niederwieser
 */
@Testable
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

  private final ISpecificationContext specificationContext = new SpecificationContext();

  /**
   * Returns the current execution context of this specification.
   * This is mostly used internally, but could, for example, be used to log the current iteration.
   */
  @Beta
  public ISpecificationContext getSpecificationContext() {
    return specificationContext;
  }

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
   * @param <U> type of target
   */
  @Beta
  public <U> void with(
    @DelegatesTo.Target
    U target,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST) @ClosureParams(FirstParam.class)
    Closure<?> closure
  ) {
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
   * @param <U> type of target
   */
  @Beta
  public <U> void with(
    Object target,
    @DelegatesTo.Target
      Class<U> type,
    @DelegatesTo(genericTypeIndex = 0, strategy = Closure.DELEGATE_FIRST)
    @ClosureParams(SecondParam.FirstGenericType.class)
      Closure closure) {
    if (target != null && !type.isInstance(target)) {
      throw new SpockAssertionError(String.format("Expected target of 'with' block to have type '%s', but got '%s'.\n%s",
          type, target.getClass().getName(), target));
    }
    with(target, closure);
  }


  /**
   * All assertions in this block are executed and the errors recorded and reported at the end.
   *
   * <p>Example:
   *
   * <pre>
   * expect:
   * verifyAll {
   *   1 == 2
   *   2 == 3
   * }
   * </pre>
   *
   * This will report two errors, instead of just the first.
   *
   * @param closure a code block containing top-level conditions and/or interactions
   */
  @Beta
  public void verifyAll(Closure closure){
    GroovyRuntimeUtil.invokeClosure(closure);
  }

  /**
   * A combination of {@link #with(Object, Closure)} and {@link #verifyAll(Closure)}.
   *
   * @since 1.2
   * @param target an implicit target for conditions and/or interactions
   * @param closure a code block containing top-level conditions and/or interactions
   * @param <U> type of target
   */
  @Beta
  public <U> void verifyAll(
    @DelegatesTo.Target
      U target,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST) @ClosureParams(FirstParam.class)
      Closure<?> closure
  ){
    if (target == null) {
      throw new SpockAssertionError("Target of 'verifyAll' block must not be null");
    }
    closure.setDelegate(target); // for conditions
    closure.setResolveStrategy(Closure.DELEGATE_FIRST);
    GroovyRuntimeUtil.invokeClosure(closure, target);
  }

  /**
   * A combination of {@link #with(Object, Class, Closure)} and {@link #verifyAll(Closure)}.
   *
   * @since 1.2
   * @param target an implicit target for conditions and/or interactions
   * @param type the expected type of the target
   * @param closure a code block containing top-level conditions and/or interactions
   * @param <U> type of target
   */
  @Beta
  public <U> void verifyAll(
    Object target,
    @DelegatesTo.Target
      Class<U> type,
    @DelegatesTo(genericTypeIndex = 0, strategy = Closure.DELEGATE_FIRST)
    @ClosureParams(SecondParam.FirstGenericType.class)
      Closure closure) {
    if (target != null && !type.isInstance(target)) {
      throw new SpockAssertionError(String.format("Expected target of 'verifyAll' block to have type '%s', but got '%s'",
        type, target.getClass().getName()));
    }
    verifyAll(target, closure);
  }

  /**
   * Performs assertions on each item, collecting up failures instead of stopping at first.
   * <p>
   * Exception messages will contain a toString() of the item to identify it.
   * <p>
   * The closure can either use one or two parameters.
   * The first parameter will always be the item.
   * The second optional parameter will be the iteration index of the item.
   *
   * @param things the iterable to inspect
   * @param closure a code block containing top-level conditions
   * @param <U> type of items in things
   * @since 2.4
   */
  @Beta
  public <U> void verifyEach(
    Iterable<U> things,
    @ClosureParams(value = FromString.class, options = {"U", "U, int"})
    @DelegatesTo(type = "U", strategy = Closure.DELEGATE_FIRST)
    Closure<?> closure
  ) {
    verifyEach(things, Objects::toString, closure);
  }

  /**
   * Performs assertions on each item, collecting up failures instead of stopping at first.
   * <p>
   * Exception messages will contain the result of calling the namer for an item to identify it.
   * <p>
   * The closure can either use one or two parameters.
   * The first parameter will always be the item.
   * The second optional parameter will be the iteration index of the item.
   *
   * @param things the iterable to inspect
   * @param namer the namer function to use when rendering the exception
   * @param closure a code block containing top-level conditions
   * @param <U> type of items in things
   * @since 2.4
   */
  @Beta
  public <U> void verifyEach(
    Iterable<U> things,
    Function<? super U, ?> namer,
    @ClosureParams(value = FromString.class, options = {"U", "U, int"})
    @DelegatesTo(type = "U", strategy = Closure.DELEGATE_FIRST)
    Closure<?> closure
  ) {
    if (things == null) {
      throw new SpockAssertionError("Target of 'verifyEach' block must not be null");
    }
    if (namer == null) {
      throw new SpockAssertionError("Namer for a 'verifyEach' block must not be null");
    }
    SpockRuntime.verifyEach(things, namer, closure);
  }
}
