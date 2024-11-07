/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.spring.mock;

import org.spockframework.mock.*;
import org.spockframework.mock.runtime.BaseMockInterceptor;
import org.spockframework.mock.runtime.IProxyBasedMockInterceptor;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.util.*;
import spock.lang.Specification;

import java.lang.reflect.*;

public class DelegatingInterceptor implements IProxyBasedMockInterceptor {
  private final FieldInfo fieldInfo;
  private Object delegate;

  public DelegatingInterceptor(FieldInfo fieldInfo) {
    this.fieldInfo = fieldInfo;
  }

  @Override
  public Object intercept(Object target, Method method, Object[] arguments, IResponseGenerator realMethodInvoker) {
    if (method.getDeclaringClass() == ISpockMockObject.class) {
      return BaseMockInterceptor.handleSpockMockInterface(method, new MockObjectAdapter(this));
    }

    if (delegate == null) {
      if ("toString".equals(method.getName())) {
        return String.format("Mock for '%s.%s:%d'", fieldInfo.getParent().getName(), fieldInfo.getName(), fieldInfo.getLine());
      } else if ("equals".equals(method.getName())){
        return fieldInfo.equals(arguments[0]);
      } else if ("hashCode".equals(method.getName())) {
        return fieldInfo.hashCode();
      }
      return null;
    }

    try {
      return method.invoke(delegate, arguments);
    } catch (IllegalAccessException e) {
      return ExceptionUtil.sneakyThrow(e);
    } catch (InvocationTargetException e) {
      return ExceptionUtil.sneakyThrow(e.getTargetException());
    }
  }

  @Override
  public void attach(Specification specification) {
    delegate = fieldInfo.readValue(specification);
    Assert.notNull(delegate, "delegate may not be null after attaching to a specification.");
  }

  @Override
  public void detach() {
    delegate = null;
  }

  private static class MockObjectAdapter implements IMockObject {
    private final DelegatingInterceptor delegatingInterceptor;

    public MockObjectAdapter(DelegatingInterceptor delegatingInterceptor) {
      this.delegatingInterceptor = delegatingInterceptor;
    }

    @Override
    public void attach(Specification specification) {
      delegatingInterceptor.attach(specification);
    }

    @Override
    public void detach() {
      delegatingInterceptor.detach();
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public String getMockName() {
      return null;
    }

    @Override
    public Class<?> getType() {
      return null;
    }

    @Override
    public Type getExactType() {
      return null;
    }

    @Override
    public Object getInstance() {
      return null;
    }

    @Override
    public boolean isVerified() {
      return false;
    }

    @Override
    public IDefaultResponse getDefaultResponse() {
      return null;
    }

    @Override
    public Specification getSpecification() {
      return null;
    }

    @Override
    public boolean matches(Object target, IMockInteraction interaction) {
      return false;
    }

    @Override
    public IMockConfiguration getConfiguration() {
      throw new UnsupportedOperationException();
    }
  }
}
