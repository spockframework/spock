package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IStatelessAnnotationDrivenExtension;
import org.spockframework.runtime.model.*;
import org.spockframework.runtime.model.parallel.ExclusiveResource;
import spock.lang.ResourceLock;

import java.util.List;

/**
 * @since 2.0
 */
public class ResourceLockExtension implements IStatelessAnnotationDrivenExtension<ResourceLock> {
  @Override
  public void visitFeatureAnnotations(List<ResourceLock> annotations, FeatureInfo feature) {
    annotations.forEach( lockResource -> feature.addExclusiveResource(
      toExclusiveResource(lockResource)));
  }

  @Override
  public void visitSpecAnnotations(List<ResourceLock> annotations, SpecInfo spec) {
    annotations.forEach( lockResource -> spec.getBottomSpec().addExclusiveResource(
      toExclusiveResource(lockResource)));
  }

  private ExclusiveResource toExclusiveResource(ResourceLock lockResource) {
    return new ExclusiveResource(lockResource.value(), lockResource.mode());
  }
}
