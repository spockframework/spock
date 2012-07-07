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
}
