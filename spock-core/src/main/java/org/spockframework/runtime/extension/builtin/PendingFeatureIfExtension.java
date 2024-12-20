package org.spockframework.runtime.extension.builtin;

import groovy.lang.Closure;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.extension.IStatelessAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import spock.lang.PendingFeatureIf;

public class PendingFeatureIfExtension extends ConditionalExtension<PendingFeatureIf> implements IStatelessAnnotationDrivenExtension<PendingFeatureIf> {

  private static final String PENDING_FEATURE_IF = "@PendingFeatureIf";

  @Override
  protected Class<? extends Closure> getConditionClass(PendingFeatureIf annotation) {
    return annotation.value();
  }

  @Override
  protected void featureConditionResult(boolean result, PendingFeatureIf annotation, FeatureInfo feature) {
    if (result) {
      if (feature.isParameterized()) {
        feature.addInterceptor(new PendingFeatureIterationInterceptor(annotation.exceptions(), annotation.reason(), PENDING_FEATURE_IF));
      } else {
        feature.getFeatureMethod().addInterceptor(
          new PendingFeatureInterceptor(annotation.exceptions(), annotation.reason(), PENDING_FEATURE_IF));
      }
    }
  }

  @Override
  protected void iterationConditionResult(boolean result, PendingFeatureIf annotation, IMethodInvocation invocation) throws Throwable {
    if (result) {
      new PendingFeatureInterceptor(annotation.exceptions(), annotation.reason(), PENDING_FEATURE_IF).intercept(invocation);
    }
  }
}
