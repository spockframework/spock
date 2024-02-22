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

package org.spockframework.lang;

import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SecondParam;
import groovy.transform.stc.ThirdParam;
import org.spockframework.mock.*;
import org.spockframework.mock.runtime.*;
import org.spockframework.runtime.*;
import org.spockframework.util.*;
import spock.lang.Specification;

import java.lang.reflect.Type;
import java.util.*;

import groovy.lang.Closure;
import spock.mock.IMockMakerSettings;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.spockframework.util.ObjectUtil.uncheckedCast;

@SuppressWarnings("UnusedDeclaration")
public abstract class SpecInternals {
  private static final MockUtil MOCK_UTIL = new MockUtil();
  private final ISpecificationContext specificationContext = new SpecificationContext();

  @Beta
  public ISpecificationContext getSpecificationContext() {
    return specificationContext;
  }

  @Beta
  public <T> T createMock(@Nullable String name, Type type, MockNature nature,
      MockImplementation implementation, Map<String, Object> options, @Nullable Closure<?> closure) {
    return createMock(name, null, type, nature, implementation, options, closure);
  }

  @Beta
  public <T> T createMock(@Nullable String name, T instance, Type type, MockNature nature,
      MockImplementation implementation, Map<String, Object> options, @Nullable Closure<?> closure) {
    Object mock = CompositeMockFactory.INSTANCE.create(
        new MockConfiguration(name, type, instance, nature, implementation, options), (Specification) this);
    if (closure != null) {
      GroovyRuntimeUtil.invokeClosure(closure, mock);
    }
    return uncheckedCast(mock);
  }

  private void createStaticMock(Type type, MockNature nature,
                                Map<String, Object> options) {
    MockConfiguration configuration = new MockConfiguration(null, type, null, nature, MockImplementation.JAVA, options);
    JavaMockFactory.INSTANCE.createStaticMock(configuration, (Specification) this);
  }

  <T> T oldImpl(T expression) {
    return expression;
  }

  <T extends Throwable> T thrownImpl(String inferredName, Class<T> inferredType) {
    return inferredType.cast(checkExceptionThrown(inferredType));
  }

  Throwable thrownImpl(String inferredName, Class<? extends Throwable> inferredType, Class<? extends Throwable> specifiedType) {
    return checkExceptionThrown(specifiedType == null ? inferredType : specifiedType);
  }

  Throwable checkExceptionThrown(Class<? extends Throwable> exceptionType) {
    if (exceptionType == null) {
      throw new InvalidSpecException("Thrown exception type cannot be inferred automatically. " +
        "Please specify a type explicitly (e.g. 'thrown(MyException)').");
    }

    if (!Throwable.class.isAssignableFrom(exceptionType))
      throw new InvalidSpecException(
        "Invalid exception condition: '%s' is not a (subclass of) java.lang.Throwable"
      ).withArgs(exceptionType.getSimpleName());

    Throwable actual = specificationContext.getThrownException();
    if (exceptionType.isInstance(actual)) return actual;

    throw new WrongExceptionThrownError(exceptionType, actual);
  }

  <T> T MockImpl(String inferredName, Class<T> inferredType) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, emptyMap(), null, null);
  }

  <T> T MockImpl(String inferredName, Class<T> inferredType, @ClosureParams(SecondParam.FirstGenericType.class) Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, emptyMap(), null, closure);
  }

  <T> T MockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, options, null, null);
  }

  <T> T MockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, options, null, closure);
  }

  <T> T MockImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, emptyMap(), specifiedType, null);
  }

  <T> T MockImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, emptyMap(), specifiedType, closure);
  }

  <T> T MockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, options, specifiedType, null);
  }

  <T> T MockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, options, specifiedType, closure);
  }

  <T> T StubImpl(String inferredName, Class<T> inferredType) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, emptyMap(), null, null);
  }

  <T> T StubImpl(String inferredName, Class<T> inferredType, @ClosureParams(SecondParam.FirstGenericType.class) Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, emptyMap(), null, closure);
  }

  <T> T StubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, options, null, null);
  }

  <T> T StubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, options, null, closure);
  }

  <T> T StubImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, emptyMap(), specifiedType, null);
  }

  <T> T StubImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, emptyMap(), specifiedType, closure);
  }

  <T> T StubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, options, specifiedType, null);
  }

  <T> T StubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, options, specifiedType, closure);
  }

  <T> T SpyImpl(String inferredName, Class<T> inferredType) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, emptyMap(), null, null);
  }

  <T> T SpyImpl(String inferredName, Class<T> inferredType, @ClosureParams(SecondParam.FirstGenericType.class) Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, emptyMap(), null, closure);
  }

  <T> T SpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, options, null, null);
  }

  <T> T SpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, options, null, closure);
  }

  <T> T SpyImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, emptyMap(), specifiedType, null);
  }

  <T> T SpyImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, emptyMap(), specifiedType, closure);
  }

  <T> T SpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, options, specifiedType, null);
  }

  <T> T SpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, options, specifiedType, closure);
  }

  <T> T SpyImpl(String inferredName, Class<?> inferredType, T instance) {
    return SpyImpl(inferredName, inferredType, instance, null);
  }

  <T> T SpyImpl(String inferredName, Class<?> inferredType, T instance, @ClosureParams(ThirdParam.class) Closure<?> closure) {
    if (instance == null) {
      throw new SpockException("Spy instance may not be null");
    }
    if (MOCK_UTIL.isMock(instance)) {
      throw new SpockException("Spy instance may not be another mock object.");
    }
    return createMockImpl(inferredName, uncheckedCast(instance.getClass()), instance, MockNature.SPY, MockImplementation.JAVA, singletonMap("useObjenesis", true), null, closure);
  }

  <T> T GroovyMockImpl(String inferredName, Class<T> inferredType) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, emptyMap(), null, null);
  }

  <T> T GroovyMockImpl(String inferredName, Class<T> inferredType, @ClosureParams(SecondParam.FirstGenericType.class) Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, emptyMap(), null, closure);
  }

  <T> T GroovyMockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, options, null, null);
  }

  <T> T GroovyMockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, options, null, closure);
  }

  <T> T GroovyMockImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, emptyMap(), specifiedType, null);
  }

  <T> T GroovyMockImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, emptyMap(), specifiedType, closure);
  }

  <T> T GroovyMockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, options, specifiedType, null);
  }

  <T> T GroovyMockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, options, specifiedType, closure);
  }

  <T> T GroovyStubImpl(String inferredName, Class<T> inferredType) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, emptyMap(), null, null);
  }

  <T> T GroovyStubImpl(String inferredName, Class<T> inferredType, @ClosureParams(SecondParam.FirstGenericType.class) Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, emptyMap(), null, closure);
  }

  <T> T GroovyStubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, options, null, null);
  }

  <T> T GroovyStubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, options, null, closure);
  }

  <T> T GroovyStubImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, emptyMap(), specifiedType, null);
  }

  <T> T GroovyStubImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, emptyMap(), specifiedType, closure);
  }

  <T> T GroovyStubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, options, specifiedType, null);
  }

  <T> T GroovyStubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, options, specifiedType, closure);
  }

  <T> T GroovySpyImpl(String inferredName, Class<T> inferredType) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, emptyMap(), null, null);
  }

  <T> T GroovySpyImpl(String inferredName, Class<T> inferredType, @ClosureParams(SecondParam.FirstGenericType.class) Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, emptyMap(), null, closure);
  }

  <T> T GroovySpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, options, null, null);
  }

  <T> T GroovySpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, options, null, closure);
  }

  <T> T GroovySpyImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, emptyMap(), specifiedType, null);
  }

  <T> T GroovySpyImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, emptyMap(), specifiedType, closure);
  }

  <T> T GroovySpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, options, specifiedType, null);
  }

  <T> T GroovySpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, options, specifiedType, closure);
  }

  private <T> T createMockImpl(String inferredName, Class<?> inferredType, MockNature nature,
      MockImplementation implementation, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    return createMockImpl(inferredName, inferredType, null, nature, implementation, options, specifiedType, closure);
  }

  private <T> T createMockImpl(String inferredName, Class<?> inferredType, T instance, MockNature nature,
      MockImplementation implementation, Map<String, Object> options, Class<?> specifiedType, Closure<?> closure) {
    Type effectiveType = specifiedType != null ? specifiedType : options.containsKey("type") ? (Type) options.get("type") : inferredType;
    if (effectiveType == null) {
      throw new InvalidSpecException("Mock object type cannot be inferred automatically. " +
          "Please specify a type explicitly (e.g. 'Mock(Person)').");
    }
    return createMock(inferredName, instance, effectiveType, nature, implementation, options, closure);
  }

  void SpyStaticImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    createStaticMockImpl(MockNature.SPY, specifiedType, null);
  }

  void SpyStaticImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, IMockMakerSettings mockMakerSettings) {
    createStaticMockImpl(MockNature.SPY, specifiedType, mockMakerSettings);
  }

  private void createStaticMockImpl(MockNature nature, Class<?> specifiedType, @Nullable IMockMakerSettings mockMakerSettings) {
    if (specifiedType == null) {
      throw new InvalidSpecException("The type must not be null.");
    }
    Map<String, Object> options = emptyMap();
    if (mockMakerSettings != null) {
      options = Collections.singletonMap("mockMaker", mockMakerSettings);
    }
    createStaticMock(specifiedType, nature, options);
  }
}
