package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.function.BiConsumer;

/**
 * @author dqyuan
 * @since 2.0
 */
public class TempDirIterationInterceptor extends TempDirBaseInterceptor {

  TempDirIterationInterceptor(Class<?> fieldType, BiConsumer<Object, Object> fieldSetter, String parentDir, boolean reserveAfterTest) {
    super(fieldType, fieldSetter, parentDir, reserveAfterTest);
  }

  @Override
  void install(SpecInfo specInfo) {
    for (FeatureInfo featureInfo : specInfo.getAllFeatures()) {
      featureInfo.addIterationInterceptor(this);
    }
  }

  @Override
  public void interceptIterationExecution(IMethodInvocation invocation) throws Throwable {
    invoke(invocation);
  }

}
