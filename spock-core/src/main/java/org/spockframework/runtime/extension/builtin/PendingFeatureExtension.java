package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import spock.lang.PendingFeature;

/**
 * @author Leonard Brünings
 */
public class PendingFeatureExtension extends AbstractAnnotationDrivenExtension<PendingFeature> {

  private static final String PENDING_FEATURE = "@PendingFeature";

  @Override
  public void visitFeatureAnnotation(PendingFeature annotation, FeatureInfo feature) {
    if (feature.isParameterized()) {
      feature.addInterceptor(new PendingFeatureIterationInterceptor(
        annotation.exceptions(), annotation.reason(), PENDING_FEATURE,
        feature.getInterceptors().stream().noneMatch(PendingFeatureIterationInterceptor.class::isInstance)));
    } else {
      feature.getFeatureMethod().addInterceptor(
        new PendingFeatureInterceptor(
          annotation.exceptions(), annotation.reason(), PENDING_FEATURE,
          feature.getFeatureMethod().getInterceptors().stream().noneMatch(PendingFeatureInterceptor.class::isInstance)));
    }
  }

}
