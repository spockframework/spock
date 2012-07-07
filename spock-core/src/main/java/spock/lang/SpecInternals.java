package spock.lang;

import java.util.Collections;
import java.util.Map;

import groovy.lang.Closure;

import org.spockframework.mock.CompositeMockFactory;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.SpecificationContext;
import org.spockframework.runtime.WrongExceptionThrownError;
import org.spockframework.runtime.GroovyRuntimeUtil;

import spock.mock.MockConfiguration;

@SuppressWarnings("UnusedDeclaration")
public abstract class SpecInternals {
  final ISpecificationContext specificationContext = new SpecificationContext();

  Throwable thrownImpl(String inferredName, Class<? extends Throwable> inferredType) {
    return thrownImpl(inferredName, inferredType, null);
  }

  Throwable thrownImpl(String inferredName, Class<? extends Throwable> inferredType, Class<? extends Throwable> specifiedType) {
    if (specifiedType == null && inferredType == null) {
      throw new InvalidSpecException("Thrown exception type cannot be inferred automatically. " +
          "Please specify a type explicitly (e.g. 'thrown(MyException)').");
    }

    Class<? extends Throwable> effectiveType = specifiedType != null ? specifiedType : inferredType;

    if (!Throwable.class.isAssignableFrom(effectiveType))
      throw new InvalidSpecException(
          "Invalid exception condition: '%s' is not a (subclass of) java.lang.Throwable"
      ).withArgs(effectiveType.getSimpleName());

    Throwable exception = specificationContext.getThrownException();
    if (effectiveType.isInstance(exception)) return exception;

    throw new WrongExceptionThrownError(effectiveType, exception);
  }

  <T> T oldImpl(T expression) {
    return expression;
  }

  Object MockImpl(String inferredName, Class<?> inferredType) {
    return createMock(inferredName, inferredType, "mock", "java", Collections.<String, Object>emptyMap(), null, null);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Closure closure) {
    return createMock(inferredName, inferredType, "mock", "java", Collections.<String, Object>emptyMap(), null, closure);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMock(inferredName, inferredType, "mock", "java", options, null, null);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure closure) {
    return createMock(inferredName, inferredType, "mock", "java", options, null, closure);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, "mock", "java", Collections.<String, Object>emptyMap(), specifiedType, null);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, "mock", "java", Collections.<String, Object>emptyMap(), specifiedType, closure);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, "mock", "java", options, specifiedType, null);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, "mock", "java", options, specifiedType, closure);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType) {
    return createMock(inferredName, inferredType, "mock", "groovy", Collections.<String, Object>emptyMap(), null, null);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Closure closure) {
    return createMock(inferredName, inferredType, "mock", "groovy", Collections.<String, Object>emptyMap(), null, closure);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMock(inferredName, inferredType, "mock", "groovy", options, null, null);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure closure) {
    return createMock(inferredName, inferredType, "mock", "groovy", options, null, closure);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, "mock", "groovy", Collections.<String, Object>emptyMap(), specifiedType, null);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, "mock", "groovy", Collections.<String, Object>emptyMap(), specifiedType, closure);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, "mock", "groovy", options, specifiedType, null);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, "mock", "groovy", options, specifiedType, closure);
  }

  private Object createMock(String inferredName, Class<?> inferredType, String nature, String impl,
      Map<String, Object> options, Class<?> specifiedType, Closure closure) {
    if (specifiedType == null && inferredType == null) {
      throw new InvalidSpecException("Mock object type cannot be inferred automatically. " +
          "Please specify a type explicitly (e.g. 'Mock(Person)').");
    }

    Class<?> effectiveType = specifiedType != null ? specifiedType : inferredType;
    Object mock = CompositeMockFactory.INSTANCE.create(
        new MockConfiguration(inferredName, effectiveType, nature, impl, options), (Specification) this);
    if (closure != null) {
      GroovyRuntimeUtil.invokeClosure(closure, mock);
    }
    return mock;
  }
}
