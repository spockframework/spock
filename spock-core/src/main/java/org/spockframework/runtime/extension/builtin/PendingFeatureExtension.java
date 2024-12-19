package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IStatelessAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import spock.lang.PendingFeature;

/**
 * @author Leonard Brünings
 */
public class PendingFeatureExtension implements IStatelessAnnotationDrivenExtension<PendingFeature> {

  private static final String PENDING_FEATURE = "@PendingFeature";

  @Override
  public void visitFeatureAnnotation(PendingFeature annotation, FeatureInfo feature) {
    if (feature.isParameterized()) {
      feature.addInterceptor(new PendingFeatureIterationInterceptor(annotation.exceptions(), annotation.reason(), PENDING_FEATURE));
    } else {
      feature.getFeatureMethod().addInterceptor(
        new PendingFeatureInterceptor(annotation.exceptions(), annotation.reason(), PENDING_FEATURE));
    }
  }

}
