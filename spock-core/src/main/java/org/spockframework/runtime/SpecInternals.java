/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.runtime;

import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SecondParam;
import groovy.transform.stc.ThirdParam;
import org.spockframework.mock.*;
import org.spockframework.mock.runtime.*;
import org.spockframework.util.*;
import spock.lang.Specification;

import java.lang.reflect.Type;
import java.util.*;

import groovy.lang.Closure;
import spock.mock.IMockMakerSettings;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.spockframework.util.ObjectUtil.uncheckedCast;

/**
 * Internal class for {@link Specification} implementation methods used by AST-transformed code.
 *
 * <p>This class is not intended for direct use.
 *
 * @since 2.4
 */
@SuppressWarnings("UnusedDeclaration")
public final class SpecInternals {
  private SpecInternals() {
  }

  private static final MockUtil MOCK_UTIL = new MockUtil();

  public static <T> T createMock(Specification specification, @Nullable String name, Type type, MockNature nature,
      MockImplementation implementation, Map<String, Object> options, @Nullable Closure<?> closure) {
    return createMock(specification, name, null, type, nature, implementation, options, closure);
  }

  public static <T> T createMock(Specification specification, @Nullable String name, T instance, Type type, MockNature nature,
      MockImplementation implementation, Map<String, Object> options, @Nullable Closure<?> closure) {
    Object mock = CompositeMockFactory.INSTANCE.create(
        new MockConfiguration(name, type, instance, nature, implementation, options), specification);
    if (closure != null) {
      GroovyRuntimeUtil.invokeClosure(closure, mock);
    }
    return uncheckedCast(mock);
  }

  public static void createStaticMock(Specification specification, Type type, MockNature nature,
                                      Map<String, Object> options) {
    MockConfiguration configuration = new MockConfiguration(null, type, null, nature, MockImplementation.JAVA, options);
    JavaMockFactory.INSTANCE.createStaticMock(configuration, specification);
  }

  public static <T> T oldImpl(T expression) {
    return expression;
  }

  public static <T extends Throwable> T thrownImpl(Specification specification, String inferredName, Class<T> inferredType) {
    return inferredType.cast(checkExceptionThrown(specification, inferredType));
  }

  public static Throwable thrownImpl(Specification specification, String inferredName, Class<? extends Throwable> inferredType, Class<? extends Throwable> specifiedType) {
    return checkExceptionThrown(specification, specifiedType == null ? inferredType : specifiedType);
  }

  public static Throwable checkExceptionThrown(Specification specification, Class<? extends Throwable> exceptionType) {
    if (exceptionType == null) {
      throw new InvalidSpecException("Thrown exception type cannot be inferred automatically. " +
        "Please specify a type explicitly (e.g. 'thrown(MyException)').");
    }

    if (!Throwable.class.isAssignableFrom(exceptionType))
      throw new InvalidSpecException(
        "Invalid exception condition: '%s' is not a (subclass of) java.lang.Throwable"
      ).withArgs(exceptionType.getSimpleName());

    SpecificationContext specificationContext = uncheckedCast(specification.getSpecificationContext());
    Throwable actual = specificationContext.getThrownException();
    if (exceptionType.isInstance(actual)) return actual;

    throw new WrongExceptionThrownError(exceptionType, actual);
  }

  public static <T> T MockImpl(Specification specification, String inferredName, Class<T> inferredType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, emptyMap(), null, null);
  }

  public static <T> T MockImpl(Specification specification, String inferredName, Class<T> inferredType, @ClosureParams(SecondParam.FirstGenericType.class) Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, emptyMap(), null, closure);
  }

  public static <T> T MockImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, options, null, null);
  }

  public static <T> T MockImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, options, null, closure);
  }

  public static <T> T MockImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, emptyMap(), specifiedType, null);
  }

  public static <T> T MockImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, emptyMap(), specifiedType, closure);
  }

  public static <T> T MockImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, options, specifiedType, null);
  }

  public static <T> T MockImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, options, specifiedType, closure);
  }

  public static <T> T StubImpl(Specification specification, String inferredName, Class<T> inferredType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, emptyMap(), null, null);
  }

  public static <T> T StubImpl(Specification specification, String inferredName, Class<T> inferredType, @ClosureParams(SecondParam.FirstGenericType.class) Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, emptyMap(), null, closure);
  }

  public static <T> T StubImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, options, null, null);
  }

  public static <T> T StubImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, options, null, closure);
  }

  public static <T> T StubImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, emptyMap(), specifiedType, null);
  }

  public static <T> T StubImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, emptyMap(), specifiedType, closure);
  }

  public static <T> T StubImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, options, specifiedType, null);
  }

  public static <T> T StubImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, options, specifiedType, closure);
  }

  public static <T> T SpyImpl(Specification specification, String inferredName, Class<T> inferredType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, emptyMap(), null, null);
  }

  public static <T> T SpyImpl(Specification specification, String inferredName, Class<T> inferredType, @ClosureParams(SecondParam.FirstGenericType.class) Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, emptyMap(), null, closure);
  }

  public static <T> T SpyImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, options, null, null);
  }

  public static <T> T SpyImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, options, null, closure);
  }

  public static <T> T SpyImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, emptyMap(), specifiedType, null);
  }

  public static <T> T SpyImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, emptyMap(), specifiedType, closure);
  }

  public static <T> T SpyImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, options, specifiedType, null);
  }

  public static <T> T SpyImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, options, specifiedType, closure);
  }

  public static <T> T SpyImpl(Specification specification, String inferredName, Class<?> inferredType, T instance) {
    return SpyImpl(specification, inferredName, inferredType, instance, null);
  }

  public static <T> T SpyImpl(Specification specification, String inferredName, Class<?> inferredType, T instance, @ClosureParams(ThirdParam.class) Closure<?> closure) {
    if (instance == null) {
      throw new SpockException("Spy instance may not be null");
    }
    if (MOCK_UTIL.isMock(instance)) {
      throw new SpockException("Spy instance may not be another mock object.");
    }
    return createMockImpl(specification, inferredName, uncheckedCast(instance.getClass()), instance, MockNature.SPY, MockImplementation.JAVA, singletonMap("useObjenesis", true), null, closure);
  }

  public static <T> T GroovyMockImpl(Specification specification, String inferredName, Class<T> inferredType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, emptyMap(), null, null);
  }

  public static <T> T GroovyMockImpl(Specification specification, String inferredName, Class<T> inferredType, @ClosureParams(SecondParam.FirstGenericType.class) Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, emptyMap(), null, closure);
  }

  public static <T> T GroovyMockImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, options, null, null);
  }

  public static <T> T GroovyMockImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, options, null, closure);
  }

  public static <T> T GroovyMockImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, emptyMap(), specifiedType, null);
  }

  public static <T> T GroovyMockImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, emptyMap(), specifiedType, closure);
  }

  public static <T> T GroovyMockImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, options, specifiedType, null);
  }

  public static <T> T GroovyMockImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, options, specifiedType, closure);
  }

  public static <T> T GroovyStubImpl(Specification specification, String inferredName, Class<T> inferredType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, emptyMap(), null, null);
  }

  public static <T> T GroovyStubImpl(Specification specification, String inferredName, Class<T> inferredType, @ClosureParams(SecondParam.FirstGenericType.class) Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, emptyMap(), null, closure);
  }

  public static <T> T GroovyStubImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, options, null, null);
  }

  public static <T> T GroovyStubImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, options, null, closure);
  }

  public static <T> T GroovyStubImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, emptyMap(), specifiedType, null);
  }

  public static <T> T GroovyStubImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, emptyMap(), specifiedType, closure);
  }

  public static <T> T GroovyStubImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, options, specifiedType, null);
  }

  public static <T> T GroovyStubImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, options, specifiedType, closure);
  }

  public static <T> T GroovySpyImpl(Specification specification, String inferredName, Class<T> inferredType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, emptyMap(), null, null);
  }

  public static <T> T GroovySpyImpl(Specification specification, String inferredName, Class<T> inferredType, @ClosureParams(SecondParam.FirstGenericType.class) Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, emptyMap(), null, closure);
  }

  public static <T> T GroovySpyImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, options, null, null);
  }

  public static <T> T GroovySpyImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, options, null, closure);
  }

  public static <T> T GroovySpyImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, emptyMap(), specifiedType, null);
  }

  public static <T> T GroovySpyImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, emptyMap(), specifiedType, closure);
  }

  public static <T> T GroovySpyImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, options, specifiedType, null);
  }

  public static <T> T GroovySpyImpl(Specification specification, String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, options, specifiedType, closure);
  }

  public static <T> T GroovySpyImpl(Specification specification, String inferredName, Class<?> inferredType, T instance) {
    return GroovySpyImpl(specification, inferredName, inferredType, instance, null);
  }

  public static <T> T GroovySpyImpl(Specification specification, String inferredName, Class<?> inferredType, T instance, Closure<?> closure) {
    if (instance == null) {
      throw new SpockException("GroovySpy instance may not be null");
    }
    if (MOCK_UTIL.isMock(instance)) {
      throw new SpockException("GroovySpy instance may not be another mock object.");
    }
    return createMockImpl(specification, inferredName, instance.getClass(), instance, MockNature.SPY, MockImplementation.GROOVY, singletonMap("useObjenesis", true), null, closure);
  }

  private static <T> T createMockImpl(Specification specification, String inferredName, Class<?> inferredType, MockNature nature,
      MockImplementation implementation, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(specification, inferredName, inferredType, null, nature, implementation, options, specifiedType, closure);
  }

  private static <T> T createMockImpl(Specification specification, String inferredName, Class<?> inferredType, T instance, MockNature nature,
      MockImplementation implementation, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    Type effectiveType = specifiedType != null ? specifiedType : options.containsKey("type") ? (Type) options.get("type") : inferredType;
    if (effectiveType == null) {
      throw new InvalidSpecException(nature + " object type cannot be inferred automatically. " +
          "Please specify a type explicitly (e.g. '" + nature + "(Person)').");
    }
    return createMock(specification, inferredName, instance, effectiveType, nature, implementation, options, closure);
  }

  public static void SpyStaticImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    createStaticMockImpl(specification, MockNature.SPY, specifiedType, null);
  }

  public static void SpyStaticImpl(Specification specification, String inferredName, Class<?> inferredType, Class<?> specifiedType, IMockMakerSettings mockMakerSettings) {
    createStaticMockImpl(specification, MockNature.SPY, specifiedType, mockMakerSettings);
  }

  private static void createStaticMockImpl(Specification specification, MockNature nature, Class<?> specifiedType, @Nullable IMockMakerSettings mockMakerSettings) {
    if (specifiedType == null) {
      throw new InvalidSpecException("The type must not be null.");
    }
    Map<String, Object> options = emptyMap();
    if (mockMakerSettings != null) {
      options = Collections.singletonMap("mockMaker", mockMakerSettings);
    }
    createStaticMock(specification, specifiedType, nature, options);
  }
}
