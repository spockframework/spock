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

package org.spockframework.lang;

import java.util.Collections;
import java.util.Map;

import groovy.lang.Closure;

import org.spockframework.mock.MockImplementation;
import org.spockframework.mock.MockNature;
import org.spockframework.util.Beta;
import org.spockframework.mock.CompositeMockFactory;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.SpecificationContext;
import org.spockframework.runtime.WrongExceptionThrownError;
import org.spockframework.runtime.GroovyRuntimeUtil;

import spock.lang.Specification;
import org.spockframework.mock.MockConfiguration;

@SuppressWarnings("UnusedDeclaration")
public abstract class SpecInternals {
  private final ISpecificationContext specificationContext = new SpecificationContext();

  @Beta
  public ISpecificationContext getSpecificationContext() {
    return specificationContext;
  }

  @Beta
  public Object createMock(String inferredName, Class<?> inferredType, MockNature nature,
                           MockImplementation implementation, Map<String, Object> options, Class<?> specifiedType, Closure closure) {
    if (specifiedType == null && inferredType == null) {
      throw new InvalidSpecException("Mock object type cannot be inferred automatically. " +
          "Please specify a type explicitly (e.g. 'Mock(Person)').");
    }

    Class<?> effectiveType = specifiedType != null ? specifiedType : inferredType;
    Object mock = CompositeMockFactory.INSTANCE.create(
        new MockConfiguration(inferredName, effectiveType, nature, implementation, options), (Specification) this);
    if (closure != null) {
      GroovyRuntimeUtil.invokeClosure(closure, mock);
    }
    return mock;
  }

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

  Object StubImpl(String inferredName, Class<?> inferredType) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, Collections.<String, Object>emptyMap(), null, null);
  }

  Object StubImpl(String inferredName, Class<?> inferredType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, Collections.<String, Object>emptyMap(), null, closure);
  }

  Object StubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, options, null, null);
  }

  Object StubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, options, null, closure);
  }

  Object StubImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, Collections.<String, Object>emptyMap(), specifiedType, null);
  }

  Object StubImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, Collections.<String, Object>emptyMap(), specifiedType, closure);
  }

  Object StubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, options, specifiedType, null);
  }

  Object StubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.JAVA, options, specifiedType, closure);
  }

  Object MockImpl(String inferredName, Class<?> inferredType) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, Collections.<String, Object>emptyMap(), null, null);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, Collections.<String, Object>emptyMap(), null, closure);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, options, null, null);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, options, null, closure);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, Collections.<String, Object>emptyMap(), specifiedType, null);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, Collections.<String, Object>emptyMap(), specifiedType, closure);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, options, specifiedType, null);
  }

  Object MockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.JAVA, options, specifiedType, closure);
  }

  Object SpyImpl(String inferredName, Class<?> inferredType) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, Collections.<String, Object>emptyMap(), null, null);
  }

  Object SpyImpl(String inferredName, Class<?> inferredType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, Collections.<String, Object>emptyMap(), null, closure);
  }

  Object SpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, options, null, null);
  }

  Object SpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, options, null, closure);
  }

  Object SpyImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, Collections.<String, Object>emptyMap(), specifiedType, null);
  }

  Object SpyImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, Collections.<String, Object>emptyMap(), specifiedType, closure);
  }

  Object SpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, options, specifiedType, null);
  }

  Object SpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.JAVA, options, specifiedType, closure);
  }

  Object GroovyStubImpl(String inferredName, Class<?> inferredType) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, Collections.<String, Object>emptyMap(), null, null);
  }

  Object GroovyStubImpl(String inferredName, Class<?> inferredType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, Collections.<String, Object>emptyMap(), null, closure);
  }

  Object GroovyStubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, options, null, null);
  }

  Object GroovyStubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, options, null, closure);
  }

  Object GroovyStubImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, Collections.<String, Object>emptyMap(), specifiedType, null);
  }

  Object GroovyStubImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, Collections.<String, Object>emptyMap(), specifiedType, closure);
  }

  Object GroovyStubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, options, specifiedType, null);
  }

  Object GroovyStubImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.STUB, MockImplementation.GROOVY, options, specifiedType, closure);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, Collections.<String, Object>emptyMap(), null, null);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, Collections.<String, Object>emptyMap(), null, closure);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, options, null, null);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, options, null, closure);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, Collections.<String, Object>emptyMap(), specifiedType, null);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, Collections.<String, Object>emptyMap(), specifiedType, closure);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, options, specifiedType, null);
  }

  Object GroovyMockImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.MOCK, MockImplementation.GROOVY, options, specifiedType, closure);
  }

  Object GroovySpyImpl(String inferredName, Class<?> inferredType) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, Collections.<String, Object>emptyMap(), null, null);
  }

  Object GroovySpyImpl(String inferredName, Class<?> inferredType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, Collections.<String, Object>emptyMap(), null, closure);
  }

  Object GroovySpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, options, null, null);
  }

  Object GroovySpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, options, null, closure);
  }

  Object GroovySpyImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, Collections.<String, Object>emptyMap(), specifiedType, null);
  }

  Object GroovySpyImpl(String inferredName, Class<?> inferredType, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, Collections.<String, Object>emptyMap(), specifiedType, closure);
  }

  Object GroovySpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, options, specifiedType, null);
  }

  Object GroovySpyImpl(String inferredName, Class<?> inferredType, Map<String, Object> options, Class<?> specifiedType, Closure closure) {
    return createMock(inferredName, inferredType, MockNature.SPY, MockImplementation.GROOVY, options, specifiedType, closure);
  }
}
