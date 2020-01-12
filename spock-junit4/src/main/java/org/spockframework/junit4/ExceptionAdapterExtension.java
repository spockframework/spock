package org.spockframework.junit4;

import org.spockframework.runtime.extension.AbstractGlobalExtension;
import org.spockframework.runtime.model.*;

public class ExceptionAdapterExtension extends AbstractGlobalExtension {

  private static ExceptionAdapterInterceptor exceptionAdapterInterceptor = new ExceptionAdapterInterceptor();

  @Override
  public void visitSpec(SpecInfo spec) {
    spec.getSetupMethods().forEach(it -> it.addInterceptor(exceptionAdapterInterceptor));
    spec.getCleanupMethods().forEach(it -> it.addInterceptor(exceptionAdapterInterceptor));
    spec.getSetupSpecMethods().forEach(it -> it.addInterceptor(exceptionAdapterInterceptor));
    spec.getCleanupSpecMethods().forEach(it -> it.addInterceptor(exceptionAdapterInterceptor));

    spec.getBottomSpec().getAllFeatures().forEach(this::addInterceptorToFeature);
  }

  private void addInterceptorToFeature(FeatureInfo featureInfo) {
    featureInfo.getFeatureMethod().addInterceptor(exceptionAdapterInterceptor);
    featureInfo.addIterationInterceptor(exceptionAdapterInterceptor);
  }
}
