/*
 * Copyright 2010 the original author or authors.
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

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.runtime.extension.*;
import org.spockframework.runtime.model.FieldInfo;
import spock.lang.AutoCleanup;

import java.util.*;

import static java.util.Collections.reverse;

/**
 * @author Peter Niederwieser
 */
public class AutoCleanupInterceptor implements IMethodInterceptor {
  private final List<FieldInfo> fields = new ArrayList<>();

  public void add(FieldInfo field) {
    fields.add(field);
  }

  public void install(List<IMethodInterceptor> interceptors) {
    if (fields.isEmpty()) return;

    reverse(fields);
    interceptors.add(this);
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    List<Throwable> exceptions = new ArrayList<>();

    try {
      invocation.proceed();
    } catch (Throwable t) {
      exceptions.add(t);
    }

    for (FieldInfo field : fields) {
      AutoCleanup annotation = field.getAnnotation(AutoCleanup.class);

      try {
        Object value = field.readValue(invocation.getInstance());
        if (value == null) continue;

        GroovyRuntimeUtil.invokeMethod(value, annotation.value());
      } catch (Throwable t) {
        if (!annotation.quiet()) exceptions.add(t);
      }
    }

    ExtensionUtil.throwAll("Exceptions during cleanup", exceptions);
  }
}
