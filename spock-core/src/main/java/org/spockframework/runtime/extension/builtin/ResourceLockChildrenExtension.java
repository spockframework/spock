package org.spockframework.runtime.extension.builtin;

import java.util.List;

import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.runtime.model.parallel.ExclusiveResource;

import spock.lang.ResourceLockChildren;

public class ResourceLockChildrenExtension implements IAnnotationDrivenExtension<ResourceLockChildren> {

  @Override
  public void visitSpecAnnotations(List<ResourceLockChildren> annotations, SpecInfo spec) {
    List<FeatureInfo> allFeatures = spec.getBottomSpec().getAllFeatures();
    annotations.stream()
      .map(this::toExclusiveResource)
      .forEach(lockResource ->
        allFeatures.forEach(feature -> feature.addExclusiveResource(lockResource)));
  }

  private ExclusiveResource toExclusiveResource(ResourceLockChildren lockResource) {
    return new ExclusiveResource(lockResource.value(), lockResource.mode());
  }
}
