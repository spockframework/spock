package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import spock.lang.AssertionMode;
import spock.lang.AssertionType;

public class AssertionModeExtension extends AbstractAnnotationDrivenExtension<AssertionMode> {
  @Override
  public void visitSpecAnnotation(AssertionMode annotation, SpecInfo spec) {
    for (FeatureInfo feature : spec.getAllFeatures()) {
      feature.setErrorCollectionEnabled(annotation.value() == AssertionType.CHECK_ALL_THEN_FAIL);
    }
  }

  @Override
  public void visitFeatureAnnotation(AssertionMode annotation, FeatureInfo feature) {
    feature.setErrorCollectionEnabled(annotation.value() == AssertionType.CHECK_ALL_THEN_FAIL);
  }
}