package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;

import java.util.function.BiConsumer;

/**
 * @author dqyuan
 * @since 2.0
 */
public class TempDirSharedInterceptor extends TempDirBaseInterceptor {

  TempDirSharedInterceptor(Class<?> fieldType, BiConsumer<Object, Object> fieldSetter, String parentDir, boolean reserveAfterTest) {
    super(fieldType, fieldSetter, parentDir, reserveAfterTest);
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
