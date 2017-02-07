package spock.mock;

import org.spockframework.mock.MockImplementation;
import org.spockframework.mock.MockNature;
import org.spockframework.mock.MockUtil;
import org.spockframework.util.Beta;
import org.spockframework.util.Nullable;
import spock.lang.Specification;

import java.util.Collections;
import java.util.Map;

/**
 * This factory allows the creations of mocks outside of a {@link spock.lang.Specification},
 * e.g., in a Spring configuration.
 * <p/>
 * In order to be usable those Mocks must be manually attached to the {@link spock.lang.Specification}
 * using {@link MockUtil#attachMock(Object, Specification)} and detached afterwards {@link MockUtil#detachMock(Object)}.
 */
@Beta
public class DetachedMockFactory implements MockFactory {
  /**
     * Creates a mock with the specified type. The mock name will be the types simple name.
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
    return createMock(inferNameFromType(type), type, MockNature.MOCK, Collections.<String, Object>emptyMap());
  }

  /**
     * Creates a mock with the specified options and type. The mock name will be the types simple name.
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
  public <T> T Mock(Map<String, Object> options, Class<T> type) {
    return createMock(inferNameFromType(type), type, MockNature.MOCK, options);
  }

  /**
     * Creates a stub with the specified type. The mock name will be the types simple name.
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
  public <T> T Stub(Class<T> type) {
    return createMock(inferNameFromType(type), type, MockNature.STUB, Collections.<String, Object>emptyMap());
  }

  /**
     * Creates a stub with the specified options and type. The mock name will be the types simple name.
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
  public <T> T Stub(Map<String, Object> options, Class<T> type) {
    return createMock(inferNameFromType(type), type, MockNature.STUB, options);
  }

  /**
     * Creates a spy with the specified type. The mock name will be the types simple name.
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
  public <T> T Spy(Class<T> type) {
    return createMock(inferNameFromType(type), type, MockNature.SPY, Collections.<String, Object>emptyMap());
  }
  
  @Override
  public <T> T Spy(T obj) {
    return createMock(inferNameFromType(obj.getClass()), obj, MockNature.SPY, Collections.<String, Object>emptyMap());
  }

  /**
     * Creates a spy with the specified options and type. The mock name will be the types simple name.
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
  public <T> T Spy(Map<String, Object> options, Class<T> type) {
    return createMock(inferNameFromType(type), type, MockNature.SPY, options);
  }


  @SuppressWarnings("unchecked")
  public <T> T createMock(@Nullable String name, Class<T> type, MockNature nature, Map<String, Object> options) {
    ClassLoader classLoader = type.getClassLoader();
    if (classLoader == null) {
      classLoader = ClassLoader.getSystemClassLoader();
    }
    return (T) new MockUtil().createDetachedMock(name, type, nature, MockImplementation.JAVA, options, classLoader);
  }
  
  @SuppressWarnings("unchecked")
  public <T> T createMock(@Nullable String name, T obj, MockNature nature, Map<String, Object> options) {
    ClassLoader classLoader = obj.getClass().getClassLoader();
    if (classLoader == null) {
      classLoader = ClassLoader.getSystemClassLoader();
    }
    return (T) new MockUtil().createDetachedMock(name, obj, nature, MockImplementation.JAVA, options, classLoader);
  }

  private String inferNameFromType(Class<?> type) {
    return type.getSimpleName();
  }
}
