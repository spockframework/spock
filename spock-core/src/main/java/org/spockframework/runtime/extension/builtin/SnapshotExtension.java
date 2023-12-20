/*
 * Copyright 2023 the original author or authors.
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
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.extension.ParameterResolver;
import org.spockframework.runtime.model.FieldInfo;
import org.spockframework.runtime.model.MethodInfo;
import org.spockframework.runtime.model.ParameterInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.util.Assert;
import org.spockframework.util.Checks;
import org.spockframework.util.ReflectionUtil;
import spock.lang.Snapshot;
import spock.lang.Snapshotter;

import java.nio.charset.Charset;

public class SnapshotExtension implements IAnnotationDrivenExtension<Snapshot> {
  public SnapshotExtension(SnapshotConfig config) {
    this.config = config;
    Assert.notNull(config.rootPath, "Root path must be set, when using @Snapshot");
  }

  @Override
  public void visitFieldAnnotation(Snapshot annotation, FieldInfo field) {
    Checks.checkArgument(Snapshotter.class.isAssignableFrom(field.getType()), () -> "Field must be of type Snapshotter or a valid Subtype");

    SpecInfo spec = field.getParent().getBottomSpec();
    spec.getAllFeatures().forEach(featureInfo -> featureInfo.addTestTag("snapshot"));
    spec.addSetupInterceptor(new SnapshotInterceptor(annotation, field));
  }

  @Override
  public void visitParameterAnnotation(final Snapshot annotation, ParameterInfo parameter) {
    Class<?> type = parameter.getReflection().getType();
    Checks.checkArgument(Snapshotter.class.isAssignableFrom(type), () -> "Field must be of type Snapshotter or a valid Subtype");

    MethodInfo method = parameter.getParent();
    method.getFeature().addTestTag("snapshot");
    method.addInterceptor(new ParameterResolver.Interceptor<>(parameter, (IMethodInvocation invocation) -> createSnapshotter(invocation, type, annotation)));

  }

  private Snapshotter createSnapshotter(IMethodInvocation invocation, Class<?> type, Snapshot annotation) {
    Snapshotter.Store snapshotStore = new Snapshotter.Store(invocation.getMethod().getIteration(), config.rootPath, config.updateSnapshots, annotation.extension(), Charset.forName(annotation.charset()));
    Checks.checkArgument(Snapshotter.class.isAssignableFrom(type), () -> "Target must be of type Snapshotter or a valid Subtype");
    return (Snapshotter) ReflectionUtil.newInstance(type, snapshotStore);
  }

  private final SnapshotConfig config;

  public class SnapshotInterceptor implements IMethodInterceptor {
    public SnapshotInterceptor(Snapshot annotation, FieldInfo field) {
      this.field = field;
      this.annotation = annotation;
    }

    @Override
    public void intercept(IMethodInvocation invocation) throws Throwable {
      field.writeValue(invocation.getInstance(), createSnapshotter(invocation, field.getType(), annotation));
      invocation.proceed();
    }

    private final FieldInfo field;
    private final Snapshot annotation;
  }
}
