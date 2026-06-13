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

import java.util.Locale;

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
      Object value = field.readValue(invocation.getInstance());
      if (value == null) {
        throw new SpockException(String.format(Locale.ROOT, "Cannot AutoAttach 'null' for field %s:%d",
          field.getName(), field.getLine()));
      }
      Specification specification = (Specification) invocation.getInstance();
      // check for a mock first: a mock of a type that implements
      // SpecificationAttachable must be attached as a mock, not have the
      // attach() call swallowed by its own interceptor
      if (MOCK_UTIL.isMock(value)) {
        MOCK_UTIL.attachMock(value, specification);
      } else if (value instanceof SpecificationAttachable) {
        ((SpecificationAttachable) value).attach(specification);
      } else {
        throw new SpockException(String.format(Locale.ROOT, "AutoAttach failed '%s' is neither a mock nor a SpecificationAttachable for field %s:%d",
          value, field.getName(), field.getLine()));
      }
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
      Object value = field.readValue(invocation.getInstance());
      if (MOCK_UTIL.isMock(value)) {
        MOCK_UTIL.detachMock(value);
      } else if (value instanceof SpecificationAttachable) {
        ((SpecificationAttachable) value).detach();
      }
      // a value that is neither already failed the attach interceptor with a
      // clear error; nothing to detach then
      invocation.proceed();
    }
  }
}
