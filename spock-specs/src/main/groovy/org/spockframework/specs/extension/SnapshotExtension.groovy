package org.spockframework.specs.extension

import groovy.transform.CompileStatic
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.extension.ParameterResolver
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
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

    if (snapshotterFields.size() != 0) {
      feature.addIterationInterceptor(new SnapshotFieldInterceptor(annotation, snapshotterFields))
    }
    snapshotterParameters.each { parameter ->
      feature.featureMethod.addInterceptor(
        new ParameterResolver.Interceptor(
          parameter,
          { IMethodInvocation invocation ->
            new Snapshotter(invocation.iteration, config.rootPath, config.updateSnapshots, annotation.extension())
          }
        ))
    }
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
      def fields = fields.groupBy { it.readValue(invocation.instance) == null }

      fields[false]?.first()?.tap {
        throw new IllegalStateException("Field $it.name is already set")
      }

      fields[true]?.each {
        it.writeValue(invocation.instance, new Snapshotter(invocation.iteration, config.rootPath, config.updateSnapshots, annotation.extension()))
      }

      invocation.proceed()
    }
  }
}
