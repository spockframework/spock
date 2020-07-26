package org.spockframework.runtime.extension.builtin;

import groovy.lang.Closure;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.MethodKind;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.tempdir.TempDirConfiguration;
import org.spockframework.util.Beta;
import org.spockframework.util.ConditionUtil;
import spock.lang.TempDir;

import java.io.File;
import java.nio.file.Path;

/**
 * @author dqyuan
 * @since 2.0
 */
@Beta
public class TempDirExtension implements IAnnotationDrivenExtension<TempDir> {
  private TempDirConfiguration configuration;

  @Override
  public void visitFieldAnnotation(TempDir annotation, FieldInfo field) {
    Class<?> fieldType = field.getType();
    if (!fieldType.isAssignableFrom(File.class) && !fieldType.isAssignableFrom(Path.class)) {
      throw new InvalidSpecException("@TempDir can only be used on File, Path or untyped field");
    }
    MethodKind interceptPoint = field.isShared() ? MethodKind.SPEC_EXECUTION : MethodKind.ITERATION_EXECUTION;
    TempDirInterceptor interceptor = new TempDirInterceptor(fieldType, field, interceptPoint,
      configuration.baseDir, evaluateCondition());

    // attach interceptor
    SpecInfo specInfo = field.getParent();
    if (field.isShared()) {
      specInfo.addInterceptor(interceptor);
    } else {
      for (FeatureInfo featureInfo : specInfo.getAllFeatures()) {
        featureInfo.addIterationInterceptor(interceptor);
      }
    }
  }

  private boolean evaluateCondition() {
    Closure condition = ConditionUtil.createCondition(configuration.keep);
    Object result = ConditionUtil.evaluateCondition(condition);
    return GroovyRuntimeUtil.isTruthy(result);
  }

}
