package org.spockframework.specs.extension

import groovy.transform.CompileStatic
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.util.Assert

@CompileStatic
class SnapshotExtension implements IAnnotationDrivenExtension<Snapshot> {
  private final SnapshotConfig config;

  SnapshotExtension(SnapshotConfig config) {
    this.config = config
    Assert.notNull(config.rootPath, "Root path must be set, when using @Snapshot")
  }

  @Override
  void visitFieldAnnotation(Snapshot annotation, FieldInfo field) {
    Assert.that(field.type.isAssignableFrom(Snapshotter), "Field must be of type Snapshotter")
    field.parent.bottomSpec.allFeatures*.addTestTag("snapshot")
    field.parent.bottomSpec.addSetupInterceptor(new SnapshotInterceptor(annotation, field))
  }

  @CompileStatic
  class SnapshotInterceptor implements IMethodInterceptor {
    private final FieldInfo field
    private final Snapshot annotation

    SnapshotInterceptor(Snapshot annotation, FieldInfo field) {
      this.field = field
      this.annotation = annotation
    }

    @Override
    void intercept(IMethodInvocation invocation) throws Throwable {
      field.writeValue(invocation.instance, new Snapshotter(invocation.method.iteration, config.rootPath, config.updateSnapshots, annotation.extension()))
      invocation.proceed()
    }
  }
}
