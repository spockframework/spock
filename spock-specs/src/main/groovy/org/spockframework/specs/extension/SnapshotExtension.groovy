package org.spockframework.specs.extension

import groovy.transform.CompileStatic
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.ParameterInfo
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.util.Assert

@CompileStatic
class SnapshotExtension implements IAnnotationDrivenExtension<Snapshot> {
  private final SnapshotConfig config;

  SnapshotExtension(SnapshotConfig config) {
    this.config = config
    Assert.notNull(config.rootPath, "Root path must be set, when using @Snapshot")
  }

  @Override
  void visitSpecAnnotation(Snapshot annotation, SpecInfo specInfo) {
    specInfo.features.each { visitFeatureAnnotation(annotation, it) }
  }

  @Override
  void visitFeatureAnnotation(Snapshot annotation, FeatureInfo feature) {
    def snapshotterFields = feature.spec.allFields.findAll {
      (it.type == Snapshotter) && !it.static && !it.shared
    }
    def snapshotterParameters = feature.featureMethod.parameters.findAll {
      (it.reflection.type == Snapshotter) && !(it.name in feature.dataVariables)
    }
    Assert.that(
      (snapshotterFields.size() != 0) || (snapshotterParameters.size() != 0),
      'There must be at least one Snapshotter parameter that is not a data variable or one Snapshotter field that is neither static nor @Shared'
    )

    feature.addIterationInterceptor(new SnapshotFieldInterceptor(annotation, snapshotterFields))
    feature.featureMethod.addInterceptor(new SnapshotParameterInterceptor(annotation, snapshotterParameters))
  }

  @CompileStatic
  class SnapshotFieldInterceptor implements IMethodInterceptor {
    private final List<FieldInfo> fields
    private final Snapshot annotation

    SnapshotFieldInterceptor(Snapshot annotation, List<FieldInfo> fields) {
      this.fields = fields
      this.annotation = annotation
    }

    @Override
    void intercept(IMethodInvocation invocation) throws Throwable {
      fields
        .findAll { it.readValue(invocation.instance) == null }
        .each { it.writeValue(invocation.instance, new Snapshotter(invocation.iteration, config.rootPath, config.updateSnapshots, annotation.extension())) }
      invocation.proceed()
    }
  }

  @CompileStatic
  class SnapshotParameterInterceptor implements IMethodInterceptor {
    private final List<ParameterInfo> parameters
    private final Snapshot annotation

    SnapshotParameterInterceptor(Snapshot annotation, List<ParameterInfo> parameters) {
      this.parameters = parameters
      this.annotation = annotation
    }

    @Override
    void intercept(IMethodInvocation invocation) throws Throwable {
      parameters
        .findAll { invocation.arguments[it.index] == MethodInfo.MISSING_ARGUMENT }
        .each { invocation.arguments[it.index] = new Snapshotter(invocation.iteration, config.rootPath, config.updateSnapshots, annotation.extension()) }
      invocation.proceed()
    }
  }
}
