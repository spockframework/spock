package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.InvalidSpecException;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.*;
import org.spockframework.tempdir.TempDirConfiguration;
import org.spockframework.util.*;
import spock.lang.TempDir;

import java.io.File;
import java.lang.reflect.Constructor;
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
    IThrowableFunction<Path, ?, Exception> mapper = createPathToFieldTypeMapper(fieldType);
    TempDirInterceptor interceptor = new TempDirInterceptor(mapper, field, configuration.baseDir, configuration.keep);

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

  private IThrowableFunction<Path, ?, Exception> createPathToFieldTypeMapper(Class<?> fieldType) {
    if (Path.class.isAssignableFrom(fieldType) || Object.class.equals(fieldType)) {
      return p -> p;
    }
    if (File.class.isAssignableFrom(fieldType)) {
      return Path::toFile;
    }

    try {
      return fieldType.getConstructor(Path.class)::newInstance;
    } catch (NoSuchMethodException ignore) {
      // fall through
    }
    try {
      Constructor<?> constructor = fieldType.getConstructor(File.class);
      return path -> constructor.newInstance(path.toFile());
    } catch (NoSuchMethodException ignore) {
      // fall through
    }
    throw new InvalidSpecException("@TempDir can only be used on File, Path, untyped field, " +
      "or class that takes Path or File as single constructor argument.");
  }
}
