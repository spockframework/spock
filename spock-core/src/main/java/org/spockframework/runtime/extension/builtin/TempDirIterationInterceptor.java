package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.lang.reflect.Field;

/**
 * @author dqyuan
 * @since 2.0
 */
public class TempDirIterationInterceptor extends TempDirBaseInterceptor {

  public TempDirIterationInterceptor(Class<?> fieldType, Field reflection, String parentDir, boolean reserveAfterTest) {
    super(fieldType, reflection, parentDir, reserveAfterTest);
  }

  @Override
  void install(SpecInfo specInfo) {
    for (FeatureInfo featureInfo : specInfo.getAllFeatures()) {
      featureInfo.addIterationInterceptor(this);
    }
  }

  @Override
  public void interceptIterationExecution(IMethodInvocation invocation) throws Throwable {
    setUp(invocation, false);
    try {
      invocation.proceed();
    } finally {
      destroy(invocation, false);
    }
  }

}
