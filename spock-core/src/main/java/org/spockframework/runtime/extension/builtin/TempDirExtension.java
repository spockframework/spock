package org.spockframework.runtime.extension.builtin;

import groovy.lang.Closure;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FieldInfo;
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

  @Override
  public void visitFieldAnnotation(TempDir annotation, FieldInfo field) {
    Class<?> fieldType = field.getType();
    if (!fieldType.isAssignableFrom(File.class) && !fieldType.isAssignableFrom(Path.class)) {
      throw new InvalidSpecException("@TempDir can only be used on File field or Path field");
    }
    TempDirBaseInterceptor interceptor = field.isShared() ?
      new TempDirSharedInterceptor(fieldType, field::writeValue,
        annotation.baseDir(), evaluateCondition(annotation)) :
      new TempDirIterationInterceptor(fieldType, field::writeValue,
        annotation.baseDir(), evaluateCondition(annotation));
    interceptor.install(field.getParent());
  }

  private boolean evaluateCondition(TempDir annotation) {
    Closure condition = ConditionUtil.createCondition(annotation.reserveAfterTest());
    Object result = ConditionUtil.evaluateCondition(condition);
    return GroovyRuntimeUtil.isTruthy(result);
  }

}
