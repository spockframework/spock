/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spock.mock;

import java.util.*;

import groovy.lang.DelegatesTo;
import groovy.transform.*;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import groovy.transform.stc.SecondParam;
import org.spockframework.lang.SpecInternals;
import org.spockframework.mock.*;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.util.Beta;

import groovy.lang.Closure;

/**
 * Spock's mocking API primarily consists of the following factory methods:
 *
 * <dl>
 *   <dt>Mock()</dt>
 *   <dd>Creates a general-purpose test double that supports both stubbing and mocking.</dd>
 *   <dt>Stub()</dt>
 *   <dd>Creates a test double that supports stubbing but not mocking.</dd>
 *   <dt>Spy()</dt>
 *   <dd>Creates a test double that, by default, delegates all calls to a real object. Supports both stubbing and mocking.</dd>
 *   <dt>GroovyMock()</dt>
 *   <dd>Creates a Mock() with additional, Groovy-specific features.</dd>
 *   <dt>GroovyStub()</dt>
 *   <dd>Creates a Stub() with additional, Groovy-specific features.</dd>
 *   <dt>GroovySpy() </dt>
 *   <dd>Creates a Spy() with additional, Groovy-specific features.</dd>
 * </dl>
 *
 * Each factory method accepts up to three parameters, each of which is optional (resulting in eight method overloads):
 * <dl>
 *   <dt>type</dt>
 *   <dd>The interface or class type of the mock. If not present and the mock is created as part of
 *   a variable assignment, the type is inferred from the variable's type (if possible).</dd>
 *   <dt>options</dt>
 *   <dd>Additional options for creating the mock. Typically passed as named arguments.
 *   See {@link org.spockframework.mock.IMockConfiguration} for available options.</dd>
 *   <dt>block</dt>
 *   <dd>A code block that allows to specify interactions right when creating the mock.</dd>
 * </dl>
 *
 * Some examples:
 * <pre>
 * def mock = Mock(Person)
 * Person mock = Mock()
 *
 * def spy = Spy(Person, constructorArgs: ["Fred"])
 *
 * Person stub = Stub {
 *   getName() >> "Fred"
 *   sing() >> "Tra-la-la"
 * }
 * </pre>
 */
@SuppressWarnings({"unused", "SameReturnValue"})
public class MockingApi extends SpecInternals implements MockFactory {
  /**
   * Encloses one or more interaction definitions in a <tt>then</tt> block.
   * Required when an interaction definition uses a statement that doesn't
   * match one of the following patterns, and therefore isn't automatically
   * recognized as belonging to an interaction definition:
   * <ul>
   * <li><tt>num * target.method(args)</tt></li>
   * <li><tt>target.method(args) &gt;&gt;(&gt;) result(s)</tt></li>
   * <li><tt>num * target.method(args) &gt;&gt;(&gt;) result(s)</tt></li>
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
    GroovyRuntimeUtil.invokeClosure(block);
  }

  /**
   * Creates a mock whose type and name are inferred from the left-hand side of the enclosing variable assignment.
   *
   * Example:
   *
   * <pre>
   *   Person person = Mock() // type is Person.class, name is "person"
   * </pre>
   *
   * @return a mock whose type and name are inferred from the left-hand side of the enclosing variable assignment
   */
  public <T> T Mock() {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a mock with the specified options whose type and name are inferred from the left-hand side of the
   * enclosing variable assignment.
   *
   * Example:
   *
   * <pre>
   *   Person person = Mock(name: "myPerson") // type is Person.class, name is "myPerson"
   * </pre>
   *
   * @param options optional options for creating the mock
   *
   * @return a mock with the specified options whose type and name are inferred from the left-hand side of the
   * enclosing variable assignment
   */
  @Beta
  public <T> T Mock(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class)
    })
      Map<String, Object> options) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a mock with the specified type. If enclosed in a variable assignment, the variable name will be
   * used as the mock's name.
   *
   * Example:
   *
   * <pre>
   *   def person = Mock(Person) // type is Person.class, name is "person"
   * </pre>
   *
   * @param type the interface or class type of the mock
   * @param <T> the interface or class type of the mock
   *
   * @return a mock with the specified type
   */
  @Override
  public <T> T Mock(Class<T> type) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a mock with the specified options and type. If enclosed in an variable assignment, the variable name
   * will be used as the mock's name.
   *
   * Example:
   *
   * <pre>
   *   def person = Mock(Person, name: "myPerson") // type is Person.class, name is "myPerson"
   * </pre>
   *
   * @param options optional options for creating the mock
   * @param type the interface or class type of the mock
   * @param <T> the interface or class type of the mock
   *
   * @return a mock with the specified options and type
   */
  @Override
  @Beta
  public <T> T Mock(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class)
    })
      Map<String, Object> options, Class<T> type) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a mock with the specified interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "person", returns hard-coded value for {@code name}, expects one call to {@code sing()}
   *   Person person = Mock {
   *     name >> "Fred"
   *     1 * sing()
   *   }
   * </pre>
   *
   * @param interactions a description of the mock's interactions
   *
   * @return a mock with the specified interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment
   */
  @Beta
  public <T> T Mock(Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a mock with the specified options and interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "myPerson", returns hard-coded value for {@code name}, expects one call to {@code sing()}
   *   Person person = Mock(name: "myPerson") {
   *     name >> "Fred"
   *     1 * sing()
   *   }
   * </pre>
   *
   * @param options optional options for creating the mock
   * @param interactions a description of the mock's interactions
   *
   * @return a mock with the specified options and interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment
   */
  @Beta
  public <T> T Mock(Map<String, Object> options, Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a mock with the specified type and interactions. If enclosed in a variable assignment, the variable name will be
   * used as the mock's name.
   *
   * Example:
   *
   * <pre>
   *   // name is "person", type is Person.class, returns hard-code value {@code name}, expects one call to {@code sing()}
   *   def person = Mock(Person) {
   *     name >> "Fred"
   *     1 * sing()
   *   }
   * </pre>
   *
   * @param type the interface or class type of the mock
   * @param interactions a description of the mock's interactions
   * @param <T> the interface or class type of the mock
   *
   * @return a mock with the specified type and interactions
   */
  @Beta
  public <T> T Mock(
    @DelegatesTo.Target
      Class<T> type,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
    @ClosureParams(FirstParam.FirstGenericType.class)
      Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a mock with the specified options, type, and interactions. If enclosed in a variable assignment, the
   * variable name will be used as the mock's name.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "myPerson", returns hard-coded value {@code name}, expects one call to {@code sing()}
   *   def person = Mock(Person, name: "myPerson") {
   *     name >> "Fred"
   *     1 * sing()
   *   }
   * </pre>
   *
   * @param options options for creating the mock (see {@link org.spockframework.mock.IMockConfiguration} for available options})
   * @param type the interface or class type of the mock
   * @param interactions a description of the mock's interactions
   * @param <T> the interface or class type of the mock
   *
   * @return a mock with the specified options, type, and interactions
   */
  @Beta
  public <T> T Mock(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class)
    })
      Map<String, Object> options,
    @DelegatesTo.Target
      Class<T> type,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
    @ClosureParams(SecondParam.FirstGenericType.class)
      Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a stub whose type and name are inferred from the left-hand side of the enclosing variable assignment.
   *
   * Example:
   *
   * <pre>
   *   Person person = Stub() // type is Person.class, name is "person"
   * </pre>
   *
   * @return a stub whose type and name are inferred from the left-hand side of the enclosing variable assignment
   */
  @Beta
  public <T> T Stub() {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a stub with the specified options whose type and name are inferred from the left-hand side of the
   * enclosing variable assignment.
   *
   * Example:
   *
   * <pre>
   *   Person person = Stub(name: "myPerson") // type is Person.class, name is "myPerson"
   * </pre>
   *
   * @param options optional options for creating the stub
   *
   * @return a stub with the specified options whose type and name are inferred from the left-hand side of the
   * enclosing variable assignment
   */
  @Beta
  public <T> T Stub(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class)
    })
      Map<String, Object> options) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a stub with the specified type. If enclosed in a variable assignment, the variable name will be
   * used as the stub's name.
   *
   * Example:
   *
   * <pre>
   *   def person = Stub(Person) // type is Person.class, name is "person"
   * </pre>
   *
   * @param type the interface or class type of the stub
   * @param <T> the interface or class type of the stub
   *
   * @return a stub with the specified type
   */
  @Override
  @Beta
  public <T> T Stub(Class<T> type) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a stub with the specified options and type. If enclosed in an variable assignment, the variable name
   * will be used as the stub's name.
   *
   * Example:
   *
   * <pre>
   *   def person = Stub(Person, name: "myPerson") // type is Person.class, name is "myPerson"
   * </pre>
   *
   * @param options optional options for creating the stub
   * @param type the interface or class type of the stub
   * @param <T> the interface or class type of the stub
   *
   * @return a stub with the specified options and type
   */
  @Override
  @Beta
  public <T> T Stub(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class)
    })
      Map<String, Object> options, Class<T> type) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a stub with the specified interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "person", returns hard-coded values for property {@code name} and method {@code sing()}
   *   Person person = Stub {
   *     name >> "Fred"
   *     sing() >> "Tra-la-la"
   *   }
   * </pre>
   *
   * @param interactions a description of the stub's interactions
   *
   * @return a stub with the specified interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment
   */
  @Beta
  public <T> T Stub(Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a stub with the specified options and interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "myPerson", returns hard-coded values for property {@code name} and method {@code sing()}
   *   Person person = Stub(name: "myPerson") {
   *     name >> "Fred"
   *     sing() >> "Tra-la-la"
   *   }
   * </pre>
   *
   * @param options optional options for creating the stub
   * @param interactions a description of the stub's interactions
   *
   * @return a stub with the specified options and interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment
   */
  @Beta
  public <T> T Stub(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class)
    })
      Map<String, Object> options, Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a stub with the specified type and interactions. If enclosed in a variable assignment, the variable name will be
   * used as the stub's name.
   *
   * Example:
   *
   * <pre>
   *   // name is "person", type is Person.class, returns hard-coded values for property {@code name} and method {@code sing()}
   *   def person = Stub(Person) {
   *     name >> "Fred"
   *     sing() >> "Tra-la-la"
   *   }
   * </pre>
   *
   * @param type the interface or class type of the stub
   * @param interactions a description of the stub's interactions
   * @param <T> the interface or class type of the stub
   *
   * @return a stub with the specified type and interactions
   */
  @Beta
  public <T> T Stub(
    @DelegatesTo.Target
      Class<T> type,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
    @ClosureParams(FirstParam.FirstGenericType.class)
      Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a stub with the specified options, type, and interactions. If enclosed in a variable assignment, the
   * variable name will be used as the stub's name.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "myPerson", returns hard-coded values for property {@code name} and method {@code sing()}
   *   def person = Stub(Person, name: "myPerson") {
   *     name >> "Fred"
   *     sing() >> "Tra-la-la"
   *   }
   * </pre>
   *
   * @param options options for creating the stub (see {@link org.spockframework.mock.IMockConfiguration} for available options})
   * @param type the interface or class type of the stub
   * @param interactions a description of the stub's interactions
   * @param <T> the interface or class type of the stub
   *
   * @return a stub with the specified options, type, and interactions
   */
  @Beta
  public <T> T Stub(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class)
    })
      Map<String, Object> options,
    @DelegatesTo.Target
      Class<T> type,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
    @ClosureParams(SecondParam.FirstGenericType.class)
      Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a spy whose type and name are inferred from the left-hand side of the enclosing variable assignment.
   *
   * Example:
   *
   * <pre>
   *   Person person = Spy() // type is Person.class, name is "person"
   * </pre>
   *
   * @return a spy whose type and name are inferred from the left-hand side of the enclosing variable assignment
   */
  @Beta
  public <T> T Spy() {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a spy with the specified options whose type and name are inferred from the left-hand side of the
   * enclosing variable assignment.
   *
   * Example:
   *
   * <pre>
   *   Person person = Spy(name: "myPerson") // type is Person.class, name is "myPerson"
   * </pre>
   *
   * @param options optional options for creating the spy
   *
   * @return a spy with the specified options whose type and name are inferred from the left-hand side of the
   * enclosing variable assignment
   */
  @Beta
  public <T> T Spy(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class)
    })
      Map<String, Object> options) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a spy with the specified type. If enclosed in a variable assignment, the variable name will be
   * used as the spy's name.
   *
   * Example:
   *
   * <pre>
   *   def person = Spy(Person) // type is Person.class, name is "person"
   * </pre>
   *
   * @param type the class type of the spy
   * @param <T> the class type of the spy
   *
   * @return a spy with the specified type
   */
  @Override
  @Beta
  public <T> T Spy(Class<T> type) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a spy wrapping a provided instance.
   *
   * Example:
   *
   * <pre>
   *   def person = Spy(new Person()) // type is Person.class, name is "person"
   * </pre>
   *
   * You need to use the spy returned by this method instead of the original instance,
   * otherwise interactions won't be picked up.
   *
   * @since 1.1
   *
   * @param obj the instance to spy
   * @param <T> the class type of the spy
   *
   * @return a spy wrapping the provided instance
   */
  @Override
  @Beta
  public <T> T Spy(T obj) {
      invalidMockCreation();
      return null;
  }

  /**
   * Creates a spy with the specified interactions wrapping a provided instance.
   *
   * Example:
   *
   * <pre>
   *   def person = Spy(new Person()) {
   *     name >> "Fred"
   *   }
   * </pre>
   *
   * You need to use the spy returned by this method instead of the original instance,
   * otherwise interactions won't be picked up.
   *
   * @since 2.0
   *
   * @param obj the instance to spy
   * @param interactions a description of the spy's interactions
   * @param <T> the class type of the spy
   *
   * @return a spy with the specified interactions wrapping the provided instance
   */
  @Beta
  public <T> T Spy(T obj, Closure interactions) {
      invalidMockCreation();
      return null;
  }

  /**
   * Creates a spy with the specified options and type. If enclosed in an variable assignment, the variable name
   * will be used as the spy's name.
   *
   * Example:
   *
   * <pre>
   *   def person = Spy(Person, name: "myPerson") // type is Person.class, name is "myPerson"
   * </pre>
   *
   * @param options optional options for creating the spy
   * @param type the class type of the spy
   * @param <T> the class type of the spy
   *
   * @return a spy with the specified options and type
   */
  @Override
  @Beta
  public <T> T Spy(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class)
    })
      Map<String, Object> options, Class<T> type) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a spy with the specified interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "person", returns hard-coded value for {@code name}, calls real method otherwise
   *   Person person = Spy {
   *     name >> "Fred"
   *   }
   * </pre>
   *
   * @param interactions a description of the spy's interactions
   *
   * @return a spy with the specified interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment
   */
  @Beta
  public <T> T Spy(Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a spy with the specified options and interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "myPerson", returns hard-coded value for {@code name}, calls real method otherwise
   *   Person person = Spy(name: "myPerson") {
   *     name >> "Fred"
   *   }
   * </pre>
   *
   * @param options optional options for creating the spy
   * @param interactions a description of the spy's interactions
   *
   * @return a spy with the specified options and interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment
   */
  @Beta
  public <T> T Spy(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class)
    })
      Map<String, Object> options, Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a spy with the specified type and interactions. If enclosed in a variable assignment, the variable name will be
   * used as the spy's name.
   *
   * Example:
   *
   * <pre>
   *   // name is "person", type is Person.class, returns hard-code value {@code name}, calls real method otherwise
   *   def person = Spy(Person) {
   *     name >> "Fred"
   *     1 * sing()
   *   }
   * </pre>
   *
   * @param type the class type of the spy
   * @param interactions a description of the spy's interactions
   * @param <T> the class type of the spy
   *
   * @return a spy with the specified type and interactions
   */
  @Beta
  public <T> T Spy(
    @DelegatesTo.Target
      Class<T> type,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
    @ClosureParams(FirstParam.FirstGenericType.class)
      Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a spy with the specified options, type, and interactions. If enclosed in a variable assignment, the
   * variable name will be used as the spy's name.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "myPerson", returns hard-coded value {@code name}, calls real method otherwise
   *   def person = Spy(Person, name: "myPerson") {
   *     name >> "Fred"
   *   }
   * </pre>
   *
   * @param options options for creating the spy (see {@link org.spockframework.mock.IMockConfiguration} for available options})
   * @param type the class type of the spy
   * @param interactions a description of the spy's interactions
   * @param <T> the class type of the spy
   *
   * @return a spy with the specified options, type, and interactions
   */
  @Beta
  public <T> T Spy(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class)
    })
      Map<String, Object> options,
    @DelegatesTo.Target
      Class<T> type,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
    @ClosureParams(SecondParam.FirstGenericType.class)
      Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy mock whose type and name are inferred from the left-hand side of the enclosing variable assignment.
   *
   * Example:
   *
   * <pre>
   *   Person person = GroovyMock() // type is Person.class, name is "person"
   * </pre>
   *
   * @return a Groovy mock whose type and name are inferred from the left-hand side of the enclosing variable assignment
   */
  @Beta
  public <T> T GroovyMock() {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy mock with the specified options whose type and name are inferred from the left-hand side of the
   * enclosing variable assignment.
   *
   * Example:
   *
   * <pre>
   *   Person person = GroovyMock(name: "myPerson") // type is Person.class, name is "myPerson"
   * </pre>
   *
   * @param options optional options for creating the Groovy mock
   *
   * @return a Groovy mock with the specified options whose type and name are inferred from the left-hand side of the
   * enclosing variable assignment
   */
  @Beta
  public <T> T GroovyMock(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class),
      @NamedParam(value = "global", type = Boolean.class)
    })
      Map<String, Object> options) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy mock with the specified type. If enclosed in a variable assignment, the variable name will be
   * used as the mock's name.
   *
   * Example:
   *
   * <pre>
   *   def person = GroovyMock(Person) // type is Person.class, name is "person"
   * </pre>
   *
   * @param type the interface or class type of the Groovy mock
   * @param <T> the interface or class type of the Groovy mock
   *
   * @return a Groovy mock with the specified type
   */
  @Beta
  public <T> T GroovyMock(Class<T> type) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy mock with the specified options and type. If enclosed in an variable assignment, the variable name
   * will be used as the mock's name.
   *
   * Example:
   *
   * <pre>
   *   def person = GroovyMock(Person, name: "myPerson") // type is Person.class, name is "myPerson"
   * </pre>
   *
   * @param options optional options for creating the Groovy mock
   * @param type the interface or class type of the Groovy mock
   * @param <T> the interface or class type of the Groovy mock
   *
   * @return a Groovy mock with the specified options and type
   */
  @Beta
  public <T> T GroovyMock(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class),
      @NamedParam(value = "global", type = Boolean.class)
    })
      Map<String, Object> options, Class<T> type) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy mock with the specified interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "person", returns hard-coded value for {@code name}, expects one call to {@code sing()}
   *   Person person = GroovyMock {
   *     name >> "Fred"
   *     1 * sing()
   *   }
   * </pre>
   *
   * @param interactions a description of the Groovy mock's interactions
   *
   * @return a Groovy mock with the specified interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment
   */
  @Beta
  public <T> T GroovyMock(Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy mock with the specified options and interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "myPerson", returns hard-coded value for {@code name}, expects one call to {@code sing()}
   *   Person person = GroovyMock(name: "myPerson") {
   *     name >> "Fred"
   *     1 * sing()
   *   }
   * </pre>
   *
   * @param options optional options for creating the Groovy mock
   * @param interactions a description of the Groovy mock's interactions
   *
   * @return a Groovy mock with the specified options and interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment
   */
  @Beta
  public <T> T GroovyMock(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class),
      @NamedParam(value = "global", type = Boolean.class)
    })
      Map<String, Object> options, Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy mock with the specified type and interactions. If enclosed in a variable assignment, the variable name will be
   * used as the mock's name.
   *
   * Example:
   *
   * <pre>
   *   // name is "person", type is Person.class, returns hard-code value {@code name}, expects one call to {@code sing()}
   *   def person = GroovyMock(Person) {
   *     name >> "Fred"
   *     1 * sing()
   *   }
   * </pre>
   *
   * @param type the interface or class type of the Groovy mock
   * @param interactions a description of the Groovy mock's interactions
   * @param <T> the interface or class type of the Groovy mock
   *
   * @return a Groovy mock with the specified type and interactions
   */
  @Beta
  public <T> T GroovyMock(
    @DelegatesTo.Target
      Class<T> type,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
    @ClosureParams(FirstParam.FirstGenericType.class)
      Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy mock with the specified options, type, and interactions. If enclosed in a variable assignment, the
   * variable name will be used as the mock's name.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "myPerson", returns hard-coded value {@code name}, expects one call to {@code sing()}
   *   def person = GroovyMock(Person, name: "myPerson") {
   *     name >> "Fred"
   *     1 * sing()
   *   }
   * </pre>
   *
   * @param options options for creating the Groovy mock (see {@link org.spockframework.mock.IMockConfiguration} for available options})
   * @param type the interface or class type of the mock
   * @param interactions a description of the Groovy mock's interactions
   * @param <T> the interface or class type of the Groovy mock
   *
   * @return a Groovy mock with the specified options, type, and interactions
   */
  @Beta
  public <T> T GroovyMock(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class),
      @NamedParam(value = "global", type = Boolean.class)
    })
      Map<String, Object> options,
    @DelegatesTo.Target
      Class<T> type,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
    @ClosureParams(SecondParam.FirstGenericType.class)
      Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy stub whose type and name are inferred from the left-hand side of the enclosing variable assignment.
   *
   * Example:
   *
   * <pre>
   *   Person person = GroovyStub() // type is Person.class, name is "person"
   * </pre>
   *
   * @return a Groovy stub whose type and name are inferred from the left-hand side of the enclosing variable assignment
   */
  @Beta
  public <T> T GroovyStub() {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy stub with the specified options whose type and name are inferred from the left-hand side of the
   * enclosing variable assignment.
   *
   * Example:
   *
   * <pre>
   *   Person person = GroovyStub(name: "myPerson") // type is Person.class, name is "myPerson"
   * </pre>
   *
   * @param options optional options for creating the Groovy stub
   *
   * @return a Groovy stub with the specified options whose type and name are inferred from the left-hand side of the
   * enclosing variable assignment
   */
  @Beta
  public <T> T GroovyStub(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class),
      @NamedParam(value = "global", type = Boolean.class)
    })
      Map<String, Object> options) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy stub with the specified type. If enclosed in a variable assignment, the variable name will be
   * used as the stub's name.
   *
   * Example:
   *
   * <pre>
   *   def person = GroovyStub(Person) // type is Person.class, name is "person"
   * </pre>
   *
   * @param type the interface or class type of the Groovy stub
   * @param <T> the interface or class type of the Groovy stub
   *
   * @return a Groovy stub with the specified type
   */
  @Beta
  public <T> T GroovyStub(Class<T> type) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy stub with the specified options and type. If enclosed in an variable assignment, the variable name
   * will be used as the stub's name.
   *
   * Example:
   *
   * <pre>
   *   def person = GroovyStub(Person, name: "myPerson") // type is Person.class, name is "myPerson"
   * </pre>
   *
   * @param options optional options for creating the Groovy stub
   * @param type the interface or class type of the Groovy stub
   * @param <T> the interface or class type of the Groovy stub
   *
   * @return a Groovy stub with the specified options and type
   */
  @Beta
  public <T> T GroovyStub(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class),
      @NamedParam(value = "global", type = Boolean.class)
    })
      Map<String, Object> options, Class<T> type) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy stub with the specified interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "person", returns hard-coded values for property {@code name} and method {@code sing()}
   *   Person person = GroovyStub {
   *     name >> "Fred"
   *     sing() >> "Tra-la-la"
   *   }
   * </pre>
   *
   * @param interactions a description of the Groovy stub's interactions
   *
   * @return a Groovy stub with the specified interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment
   */
  @Beta
  public <T> T GroovyStub(Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy stub with the specified options and interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "myPerson", returns hard-coded values for property {@code name} and method {@code sing()}
   *   Person person = GroovyStub(name: "myPerson") {
   *     name >> "Fred"
   *     sing() >> "Tra-la-la"
   *   }
   * </pre>
   *
   * @param options optional options for creating the Groovy stub
   * @param interactions a description of the Groovy stub's interactions
   *
   * @return a Groovy stub with the specified options and interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment
   */
  @Beta
  public <T> T GroovyStub(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class),
      @NamedParam(value = "global", type = Boolean.class)
    })
      Map<String, Object> options, Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy stub with the specified type and interactions. If enclosed in a variable assignment, the variable name will be
   * used as the stub's name.
   *
   * Example:
   *
   * <pre>
   *   // name is "person", type is Person.class, returns hard-coded values for property {@code name} and method {@code sing()}
   *   def person = GroovyStub(Person) {
   *     name >> "Fred"
   *     sing() >> "Tra-la-la"
   *   }
   * </pre>
   *
   * @param type the interface or class type of the Groovy stub
   * @param interactions a description of the Groovy stub's interactions
   * @param <T> the interface or class type of the Groovy stub
   *
   * @return a Groovy stub with the specified type and interactions
   */
  @Beta
  public <T> T GroovyStub(
    @DelegatesTo.Target
      Class<T> type,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
    @ClosureParams(FirstParam.FirstGenericType.class)
      Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy stub with the specified options, type, and interactions. If enclosed in a variable assignment, the
   * variable name will be used as the stub's name.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "myPerson", returns hard-coded values for property {@code name} and method {@code sing()}
   *   def person = GroovyStub(Person, name: "myPerson") {
   *     name >> "Fred"
   *     sing() >> "Tra-la-la"
   *   }
   * </pre>
   *
   * @param options options for creating the Groovy stub (see {@link org.spockframework.mock.IMockConfiguration} for available options})
   * @param type the interface or class type of the Groovy stub
   * @param interactions a description of the Groovy stub's interactions
   * @param <T> the interface or class type of the Groovy stub
   *
   * @return a Groovy stub with the specified options, type, and interactions
   */
  @Beta
  public <T> T GroovyStub(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class),
      @NamedParam(value = "global", type = Boolean.class)
    })
      Map<String, Object> options,
    @DelegatesTo.Target
      Class<T> type,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
    @ClosureParams(SecondParam.FirstGenericType.class)
      Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy spy whose type and name are inferred from the left-hand side of the enclosing variable assignment.
   *
   * Example:
   *
   * <pre>
   *   Person person = GroovySpy() // type is Person.class, name is "person"
   * </pre>
   *
   * @return a Groovy spy whose type and name are inferred from the left-hand side of the enclosing variable assignment
   */
  @Beta
  public <T> T GroovySpy() {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy spy with the specified options whose type and name are inferred from the left-hand side of the
   * enclosing variable assignment.
   *
   * Example:
   *
   * <pre>
   *   Person person = GroovySpy(name: "myPerson") // type is Person.class, name is "myPerson"
   * </pre>
   *
   * @param options optional options for creating the Groovy spy
   *
   * @return a Groovy spy with the specified options whose type and name are inferred from the left-hand side of the
   * enclosing variable assignment
   */
  @Beta
  public <T> T GroovySpy(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class),
      @NamedParam(value = "global", type = Boolean.class)
    })
      Map<String, Object> options) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy spy with the specified type. If enclosed in a variable assignment, the variable name will be
   * used as the spy's name.
   *
   * Example:
   *
   * <pre>
   *   def person = GroovySpy(Person) // type is Person.class, name is "person"
   * </pre>
   *
   * @param type the class type of the Groovy spy
   * @param <T> the class type of the Groovy spy
   *
   * @return a Groovy spy with the specified type
   */
  @Beta
  public <T> T GroovySpy(Class<T> type) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy spy with the specified options and type. If enclosed in an variable assignment, the variable name
   * will be used as the spy's name.
   *
   * Example:
   *
   * <pre>
   *   def person = GroovySpy(Person, name: "myPerson") // type is Person.class, name is "myPerson"
   * </pre>
   *
   * @param options optional options for creating the Groovy spy
   * @param type the class type of the Groovy spy
   * @param <T> the class type of the Groovy spy
   *
   * @return a Groovy spy with the specified options and type
   */
  @Beta
  public <T> T GroovySpy(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class),
      @NamedParam(value = "global", type = Boolean.class)
    })
      Map<String, Object> options, Class<T> type) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy spy with the specified interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "person", returns hard-coded value for {@code name}, calls real method otherwise
   *   Person person = GroovySpy {
   *     name >> "Fred"
   *   }
   * </pre>
   *
   * @param interactions a description of the spy's interactions
   *
   * @return a Groovy spy with the specified interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment
   */
  @Beta
  public <T> T GroovySpy(Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy spy with the specified options and interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "myPerson", returns hard-coded value for {@code name}, calls real method otherwise
   *   Person person = GroovySpy(name: "myPerson") {
   *     name >> "Fred"
   *   }
   * </pre>
   *
   * @param options optional options for creating the Groovy spy
   * @param interactions a description of the Groovy spy's interactions
   *
   * @return a Groovy spy with the specified options and interactions whose type and name are inferred
   * from the left-hand side of the enclosing assignment
   */
  @Beta
  public <T> T GroovySpy(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class),
      @NamedParam(value = "global", type = Boolean.class)
    })
      Map<String, Object> options, Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy spy with the specified type and interactions. If enclosed in a variable assignment, the variable name will be
   * used as the spy's name.
   *
   * Example:
   *
   * <pre>
   *   // name is "person", type is Person.class, returns hard-code value {@code name}, calls real method otherwise
   *   def person = GroovySpy(Person) {
   *     name >> "Fred"
   *     1 * sing()
   *   }
   * </pre>
   *
   * @param type the class type of the Groovy spy
   * @param interactions a description of the Groovy spy's interactions
   * @param <T> the class type of the Groovy spy
   *
   * @return a Groovy spy with the specified type and interactions
   */
  @Beta
  public <T> T GroovySpy(
    @DelegatesTo.Target
      Class<T> type,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
    @ClosureParams(FirstParam.FirstGenericType.class)
      Closure interactions) {
    invalidMockCreation();
    return null;
  }

  /**
   * Creates a Groovy spy with the specified options, type, and interactions. If enclosed in a variable assignment, the
   * variable name will be used as the spy's name.
   *
   * Example:
   *
   * <pre>
   *   // type is Person.class, name is "myPerson", returns hard-coded value {@code name}, calls real method otherwise
   *   def person = GroovySpy(Person, name: "myPerson") {
   *     name >> "Fred"
   *   }
   * </pre>
   *
   * @param options options for creating the Groovy spy (see {@link org.spockframework.mock.IMockConfiguration} for available options})
   * @param type the class type of the Groovy spy
   * @param interactions a description of the Groovy spy's interactions
   * @param <T> the class type of the Groovy spy
   *
   * @return a Groovy spy with the specified options, type, and interactions
   */
  @Beta
  public <T> T GroovySpy(
    @NamedParams({
      @NamedParam(value = "name", type = String.class),
      @NamedParam(value = "additionalInterfaces", type = List.class),
      @NamedParam(value = "defaultResponse", type = IDefaultResponse.class),
      @NamedParam(value = "useLastMatchResponseStrategy", type = Boolean.class),
      @NamedParam(value = "verified", type = Boolean.class),
      @NamedParam(value = "useObjenesis", type = Boolean.class),
      @NamedParam(value = "global", type = Boolean.class)
    })
      Map<String, Object> options,
    @DelegatesTo.Target
      Class<T> type,
    @DelegatesTo(strategy = Closure.DELEGATE_FIRST, genericTypeIndex = 0)
    @ClosureParams(SecondParam.FirstGenericType.class)
      Closure interactions) {
    invalidMockCreation();
    return null;
  }

  private void invalidMockCreation() {
    throw new InvalidSpecException("Mock objects can only be created inside a spec, and only during the lifetime of a feature (iteration)");
  }
}
