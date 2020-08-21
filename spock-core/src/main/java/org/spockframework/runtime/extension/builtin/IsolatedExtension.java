package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.runtime.model.parallel.ExclusiveResource;
import org.spockframework.runtime.model.parallel.ResourceAccessMode;

import spock.lang.Isolated;

public class IsolatedExtension implements IAnnotationDrivenExtension<Isolated> {

  private static final ExclusiveResource GLOBAL_LOCK = new ExclusiveResource(
    "org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_KEY", ResourceAccessMode.READ_WRITE);

  @Override
  public void visitSpec(SpecInfo spec) {
    spec.addExclusiveResource(GLOBAL_LOCK);
  }
}
