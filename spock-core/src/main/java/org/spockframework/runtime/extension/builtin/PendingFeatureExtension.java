package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import spock.lang.PendingFeature;

/**
 * @author Leonard Brünings
 */
public class PendingFeatureExtension extends AbstractAnnotationDrivenExtension<PendingFeature> {
  @Override
  public void visitFeatureAnnotation(PendingFeature annotation, FeatureInfo feature) {
    if (feature.isParameterized()) {
      feature.addInterceptor(new PendingFeatureIterationInterceptor());
    } else {
      feature.getFeatureMethod().addInterceptor(new PendingFeatureInterceptor());
    }
  }
}
