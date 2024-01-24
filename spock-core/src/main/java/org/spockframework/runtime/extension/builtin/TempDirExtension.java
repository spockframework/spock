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
import java.util.EnumSet;
import java.util.Set;

/**
 * @author dqyuan
 * @since 2.0
 */
@Beta
public class TempDirExtension implements IAnnotationDrivenExtension<TempDir> {

  private static final Set<MethodKind> VALID_METHOD_KINDS = EnumSet.of(MethodKind.SETUP, MethodKind.SETUP_SPEC, MethodKind.FEATURE);
  private final TempDirConfiguration configuration;

  public TempDirExtension(TempDirConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void visitFieldAnnotation(TempDir annotation, FieldInfo field) {
    TempDirInterceptor interceptor = TempDirInterceptor.forField(field, configuration.baseDir, configuration.keep);

    // attach interceptor
    SpecInfo specInfo = field.getParent();
    if (field.isShared()) {
      specInfo.getBottomSpec().addSharedInitializerInterceptor(interceptor);
    } else {
      specInfo.addInitializerInterceptor(interceptor);
    }
  }

  @Override
  public void visitParameterAnnotation(TempDir annotation, ParameterInfo parameter) {
    Checks.checkArgument(VALID_METHOD_KINDS.contains(parameter.getParent().getKind()), () -> "@TempDir can only be used on setup, setupSpec or feature method parameters.");
    TempDirInterceptor interceptor = TempDirInterceptor.forParameter(parameter, configuration.baseDir, configuration.keep);
    parameter.getParent().addInterceptor(interceptor);
  }
}
