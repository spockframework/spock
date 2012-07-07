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
