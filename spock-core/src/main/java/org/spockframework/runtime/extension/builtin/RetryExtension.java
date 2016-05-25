package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;
import spock.lang.Retry;

public class RetryExtension extends AbstractAnnotationDrivenExtension<Retry> {
  @Override
  public void visitFeatureAnnotation(Retry annotation, FeatureInfo feature) {
    feature.setRetryCount(getRetryCount(annotation));
  }

  @Override
  public void visitSpecAnnotation(Retry annotation, SpecInfo spec) {
    int retryCount = getRetryCount(annotation);
    for (FeatureInfo featureInfo : spec.getFeatures()) {
      featureInfo.setRetryCount(retryCount);
    }
  }

  private int getRetryCount(Retry annotation) {
    int retryCount = annotation.value();
    if (!Retry.DO_NOT_USE_SYSTEM_PROPERTY.equals(annotation.systemProperty())){
      retryCount = Integer.getInteger(annotation.systemProperty(), retryCount);
    }
    return retryCount;
  }
}
