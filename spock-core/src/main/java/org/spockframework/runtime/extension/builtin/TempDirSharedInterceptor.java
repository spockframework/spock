package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.Beta;

/**
 * @author dqyuan
 * @since 2.0
 */
@Beta
public class TempDirSharedInterceptor extends TempDirBaseInterceptor {

  TempDirSharedInterceptor(Class<?> fieldType, FieldInfo fieldInfo,
                           String parentDir, boolean reserveAfterTest) {
    super(fieldType, fieldInfo, parentDir, reserveAfterTest);
  }

  @Override
  void install(SpecInfo specInfo) {
    specInfo.addInterceptor(this);
  }

  @Override
  public void interceptSpecExecution(IMethodInvocation invocation) throws Throwable {
    invoke(invocation);
  }

}
