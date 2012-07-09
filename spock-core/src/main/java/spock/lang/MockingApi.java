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

package spock.lang;

import java.util.Map;

import groovy.lang.Closure;

import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.GroovyRuntimeUtil;

public class MockingApi extends SpecInternals {
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
    GroovyRuntimeUtil.invokeClosure(block);
  }

  public Object Stub() {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public Object Stub(Map<String, Object> options) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public <T> T Stub(Class<T> type) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public <T> T Stub(Map<String, Object> options, Class<T> type) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public Object Stub(Closure closure) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public Object Stub(Map<String, Object> options, Closure closure) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public <T> T Stub(Class<T> type, Closure closure) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public <T> T Stub(Map<String, Object> options, Class<T> type, Closure closure) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public Object Mock() {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public Object Mock(Map<String, Object> options) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public <T> T Mock(Class<T> type) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public <T> T Mock(Map<String, Object> options, Class<T> type) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public Object Mock(Closure closure) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public Object Mock(Map<String, Object> options, Closure closure) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public <T> T Mock(Class<T> type, Closure closure) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public <T> T Mock(Map<String, Object> options, Class<T> type, Closure closure) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public Object Spy() {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public Object Spy(Map<String, Object> options) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public <T> T Spy(Class<T> type) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public <T> T Spy(Map<String, Object> options, Class<T> type) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public Object Spy(Closure closure) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public Object Spy(Map<String, Object> options, Closure closure) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public <T> T Spy(Class<T> type, Closure closure) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public <T> T Spy(Map<String, Object> options, Class<T> type, Closure closure) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public Object GroovyStub() {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public Object GroovyStub(Map<String, Object> options) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public <T> T GroovyStub(Class<T> type) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public <T> T GroovyStub(Map<String, Object> options, Class<T> type) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public Object GroovyStub(Closure closure) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public Object GroovyStub(Map<String, Object> options, Closure closure) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public <T> T GroovyStub(Class<T> type, Closure closure) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public <T> T GroovyStub(Map<String, Object> options, Class<T> type, Closure closure) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public Object GroovyMock() {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public Object GroovyMock(Map<String, Object> options) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public <T> T GroovyMock(Class<T> type) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public <T> T GroovyMock(Map<String, Object> options, Class<T> type) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public Object GroovyMock(Closure closure) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public Object GroovyMock(Map<String, Object> options, Closure closure) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public <T> T GroovyMock(Class<T> type, Closure closure) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public <T> T GroovyMock(Map<String, Object> options, Class<T> type, Closure closure) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public Object GroovySpy() {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public Object GroovySpy(Map<String, Object> options) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public <T> T GroovySpy(Class<T> type) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public <T> T GroovySpy(Map<String, Object> options, Class<T> type) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public Object GroovySpy(Closure closure) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public Object GroovySpy(Map<String, Object> options, Closure closure) {
    throw new InvalidSpecException("Test Doubles may only be created during the lifetime of a feature (iteration)");
  }

  public <T> T GroovySpy(Class<T> type, Closure closure) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }

  public <T> T GroovySpy(Map<String, Object> options, Class<T> type, Closure closure) {
    throw new InvalidSpecException("Test Doubles can only be created inside a Spec");
  }
}
