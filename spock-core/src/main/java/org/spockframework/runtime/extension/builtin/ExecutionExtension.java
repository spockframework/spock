package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.*;
import spock.lang.Execution;

/**
 * @since 2.0
 */
public class ExecutionExtension implements IAnnotationDrivenExtension<Execution> {
  @Override
  public void visitSpecAnnotation(Execution annotation, SpecInfo spec) {
    spec.getBottomSpec().setExecutionMode(annotation.value());
  }

  @Override
  public void visitFeatureAnnotation(Execution annotation, FeatureInfo feature) {
    feature.setExecutionMode(annotation.value());
  }
}
