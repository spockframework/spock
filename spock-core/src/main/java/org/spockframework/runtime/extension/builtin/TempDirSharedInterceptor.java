package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;

import java.lang.reflect.Field;

/**
 * @author dqyuan
 * @since 2.0
 */
public class TempDirSharedInterceptor extends TempDirBaseInterceptor {

  public TempDirSharedInterceptor(Class<?> fieldType, Field reflection, String parentDir, boolean reserveAfterTest) {
    super(fieldType, reflection, parentDir, reserveAfterTest);
  }

  @Override
  void install(SpecInfo specInfo) {
    specInfo.addSetupSpecInterceptor(this);
    specInfo.addCleanupSpecInterceptor(this);
  }

  @Override
  public void interceptSetupSpecMethod(IMethodInvocation invocation) throws Throwable {
    setUp(invocation, true);
  }

  @Override
  public void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
    destroy(invocation, true);
  }

}
