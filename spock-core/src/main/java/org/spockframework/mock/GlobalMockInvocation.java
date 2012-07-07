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

package org.spockframework.mock;

import java.util.List;

import org.spockframework.runtime.GroovyRuntimeUtil;

public class GlobalMockInvocation extends MockInvocation {
  public GlobalMockInvocation(IMockObject mockObject, IMockMethod method, List<Object> arguments) {
    super(mockObject, method, arguments);
  }

  public Object callRealMethod() {
    return callRealMethodWith(getArguments().toArray());
  }

  public Object callRealMethodWith(Object... arguments) {
    Class<?> type = getMockObject().getType();
    GroovyMockMetaClass metaClass = (GroovyMockMetaClass) GroovyRuntimeUtil.getMetaClass(type);
    GroovyRuntimeUtil.setMetaClass(type, metaClass.getAdaptee());
    try {
      return GroovyRuntimeUtil.invokeMethod(getMockObject().getInstance(), getMethod().getName(), arguments);
    } finally {
      GroovyRuntimeUtil.setMetaClass(type, metaClass);
    }
  }
}
