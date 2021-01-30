package org.spockframework.junit4;

import org.spockframework.runtime.extension.AbstractGlobalExtension;
import org.spockframework.runtime.model.*;

public class ExceptionAdapterExtension extends AbstractGlobalExtension {

  private static final ExceptionAdapterInterceptor exceptionAdapterInterceptor = new ExceptionAdapterInterceptor();

  @Override
  public void visitSpec(SpecInfo spec) {
    spec.getAllFixtureMethods().forEach(it -> it.addInterceptor(exceptionAdapterInterceptor));

    spec.getBottomSpec().getAllFeatures().forEach(this::addInterceptorToFeature);
  }

  private void addInterceptorToFeature(FeatureInfo featureInfo) {
    featureInfo.getFeatureMethod().addInterceptor(exceptionAdapterInterceptor);
    featureInfo.addIterationInterceptor(exceptionAdapterInterceptor);
  }
}
