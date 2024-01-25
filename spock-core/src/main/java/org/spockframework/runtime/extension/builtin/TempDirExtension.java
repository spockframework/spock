/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.model.*;
import org.spockframework.tempdir.TempDirConfiguration;
import org.spockframework.util.Beta;
import org.spockframework.util.Checks;
import org.spockframework.util.UnreachableCodeError;
import spock.lang.TempDir;

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
    TempDir.CleanupMode cleanupMode = annotation.cleanup();
    cleanupMode = cleanupMode == TempDir.CleanupMode.DEFAULT ? configuration.cleanup : cleanupMode;
    TempDirInterceptor interceptor = TempDirInterceptor.forField(field, configuration.baseDir, annotation, cleanupMode);

    SpecInfo specInfo = field.getParent();
    if (field.isShared()) {
      specInfo.getBottomSpec().addSharedInitializerInterceptor(interceptor);
    } else {
      specInfo.addInitializerInterceptor(interceptor);
    }
    if (cleanupMode == TempDir.CleanupMode.ON_SUCCESS) {
      registerForAllFeatures(specInfo, new TempDirInterceptor.FailureTracker(annotation));
    }
  }

  private static void registerForAllFeatures(SpecInfo specInfo, IMethodInterceptor interceptor) {
    specInfo.getBottomSpec().getAllFeatures().forEach(featureInfo -> featureInfo.getFeatureMethod().addInterceptor(interceptor));
  }

  @Override
  public void visitParameterAnnotation(TempDir annotation, ParameterInfo parameter) {
    TempDir.CleanupMode cleanupMode = annotation.cleanup();
    cleanupMode = cleanupMode == TempDir.CleanupMode.DEFAULT ? configuration.cleanup : cleanupMode;
    MethodInfo methodInfo = parameter.getParent();
    Checks.checkArgument(VALID_METHOD_KINDS.contains(methodInfo.getKind()), () -> "@TempDir can only be used on setup, setupSpec or feature method parameters.");
    TempDirInterceptor interceptor = TempDirInterceptor.forParameter(parameter, configuration.baseDir, annotation, cleanupMode);
    methodInfo.addInterceptor(interceptor);

    if (cleanupMode == TempDir.CleanupMode.ON_SUCCESS) {
      TempDirInterceptor.FailureTracker failureTracker = new TempDirInterceptor.FailureTracker(annotation);
      switch (methodInfo.getKind()) {
        case SETUP:
        case SETUP_SPEC:
          registerForAllFeatures(methodInfo.getParent(), failureTracker);
          break;
        case FEATURE:
          methodInfo.addInterceptor(failureTracker);
          break;
        default:
          throw new UnreachableCodeError();
      }
    }
  }
}
