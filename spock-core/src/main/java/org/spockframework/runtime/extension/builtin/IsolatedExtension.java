package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IStatelessAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;
import org.spockframework.runtime.model.parallel.*;
import spock.lang.Isolated;

import java.util.List;

public class IsolatedExtension implements IStatelessAnnotationDrivenExtension<Isolated> {

  private static final ExclusiveResource GLOBAL_LOCK = new ExclusiveResource(
    org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_KEY, ResourceAccessMode.READ_WRITE);

  @Override
  public void visitSpecAnnotations(List<Isolated> annotations, SpecInfo spec) {
    spec.getBottomSpec().addExclusiveResource(GLOBAL_LOCK);
  }
}
