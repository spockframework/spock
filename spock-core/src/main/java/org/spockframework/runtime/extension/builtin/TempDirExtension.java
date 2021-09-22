package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.*;
import org.spockframework.tempdir.TempDirConfiguration;
import org.spockframework.util.Beta;
import spock.lang.TempDir;

import java.io.File;
import java.nio.file.Path;

/**
 * @author dqyuan
 * @since 2.0
 */
@Beta
public class TempDirExtension implements IAnnotationDrivenExtension<TempDir> {
  private final TempDirConfiguration configuration;

  public TempDirExtension(TempDirConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void visitFieldAnnotation(TempDir annotation, FieldInfo field) {
    Class<?> fieldType = field.getType();
    if (!fieldType.isAssignableFrom(File.class) && !fieldType.isAssignableFrom(Path.class)) {
      throw new InvalidSpecException("@TempDir can only be used on File, Path or untyped field");
    }
    TempDirInterceptor interceptor = new TempDirInterceptor(fieldType, field, configuration.baseDir, configuration.keep);

    // attach interceptor
    SpecInfo specInfo = field.getParent();
    if (field.isShared()) {
      specInfo.getBottomSpec().addInterceptor(interceptor);
    } else {
      for (FeatureInfo featureInfo : specInfo.getBottomSpec().getAllFeatures()) {
        featureInfo.addIterationInterceptor(interceptor);
      }
    }
  }

}
