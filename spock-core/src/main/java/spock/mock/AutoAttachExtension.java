/*
 * Copyright 2017 the original author or authors.
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
package spock.mock;

import org.spockframework.mock.MockUtil;
import org.spockframework.runtime.SpockException;
import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.*;
import spock.lang.Specification;

public class AutoAttachExtension implements IStatelessAnnotationDrivenExtension<AutoAttach> {
  private static final MockUtil MOCK_UTIL = new MockUtil();

  @Override
  public void visitFieldAnnotation(AutoAttach annotation, FieldInfo field) {
    if (field.isShared()) {
      throw new SpockException(String.format("@AutoAttach is only supported for instance fields (offending field: %s.%s)",
        field.getReflection().getDeclaringClass().getName(), field.getName()));
    }
    SpecInfo spec = field.getParent();
    spec.addSetupInterceptor(new AttachInterceptor(field));
    spec.addCleanupInterceptor(new DetachInterceptor(field));
  }

  private static class AttachInterceptor implements IMethodInterceptor {
    private final FieldInfo field;

    private AttachInterceptor(FieldInfo field) {
      this.field = field;
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
      Object mock = field.readValue(invocation.getInstance());
      if (mock == null) {
        throw new SpockException(String.format("Cannot AutoAttach 'null' for field %s:%s",
          field.getName(), field.getLine()));
      }
      if (!MOCK_UTIL.isMock(mock)) {
        throw new SpockException(String.format("AutoAttach failed '%s' is not a mock for field %s:%s",
          mock, field.getName(), field.getLine()));
      }
      MOCK_UTIL.attachMock(mock, (Specification) invocation.getInstance());
      invocation.proceed();
    }
  }

  private static class DetachInterceptor implements IMethodInterceptor {
    private final FieldInfo field;

    private DetachInterceptor(FieldInfo field) {
      this.field = field;
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
      Object mock = field.readValue(invocation.getInstance());
      MOCK_UTIL.detachMock(mock);
      invocation.proceed();
    }
  }
}
