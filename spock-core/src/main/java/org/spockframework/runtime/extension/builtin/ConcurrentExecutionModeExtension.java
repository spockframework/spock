package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import spock.lang.ConcurrentExecutionMode;

public class ConcurrentExecutionModeExtension extends AbstractAnnotationDrivenExtension<ConcurrentExecutionMode>{
  @Override
  public void visitSpecAnnotation(ConcurrentExecutionMode annotation, SpecInfo spec) {
    spec.setSupportParallelExecution(annotation.value() == ConcurrentExecutionMode.Mode.USE_JUNIT_SCHEDULER);
    for (FeatureInfo featureInfo : spec.getAllFeatures()) {
      visitFeatureAnnotation(annotation, featureInfo);
    }
  }

  @Override
  public void visitFeatureAnnotation(ConcurrentExecutionMode annotation, FeatureInfo feature) {
    feature.setSupportParallelExecution(annotation.value() == ConcurrentExecutionMode.Mode.USE_JUNIT_SCHEDULER);
  }
}
