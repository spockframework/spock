package spock.mock;

import java.util.Map;

/**
 * Base interface for Java based mocks see {@link MockingApi} or {@link DetachedMockFactory} for more examples.
 */
public interface MockFactory {
  /**
   * Creates a mock with the specified type.
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
  <T> T Mock(Class<T> type);

  /**
   * Creates a mock with the specified options and type.
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
  <T> T Mock(Map<String, Object> options, Class<T> type);

  /**
   * Creates a stub with the specified type.
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
  <T> T Stub(Class<T> type);

  /**
   * Creates a stub with the specified options and type.
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
  <T> T Stub(Map<String, Object> options, Class<T> type);

  /**
   * Creates a spy with the specified type.
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
  <T> T Spy(Class<T> type);
  
  /**
   * Creates a spy wrapping a provided instance.
   *
   * Example:
   *
   * <pre>
   *   def person = Spy(new Person()) // type is Person.class, name is "person"
   * </pre>
   *
   * @param obj the instance to spy
   * @param <T> the class type of the spy
   *
   * @return a spy with the specified type
   */
  <T> T Spy(T obj);

  /**
   * Creates a spy with the specified options and type.
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
  <T> T Spy(Map<String, Object> options, Class<T> type);

}
