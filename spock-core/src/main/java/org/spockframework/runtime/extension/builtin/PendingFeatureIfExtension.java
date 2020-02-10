package org.spockframework.runtime.extension.builtin;

import groovy.lang.Closure;
import org.spockframework.runtime.GroovyRuntimeUtil;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.extension.ExtensionException;
import org.spockframework.runtime.model.FeatureInfo;
import spock.lang.PendingFeatureIf;


public class PendingFeatureIfExtension extends AbstractAnnotationDrivenExtension<PendingFeatureIf> {

  private static final String PENDING_FEATURE_IF = "@PendingFeatureIf";

  @Override
  public void visitFeatureAnnotation(PendingFeatureIf annotation, FeatureInfo feature) {
    Closure condition = createCondition(annotation.value());
    Object result = evaluateCondition(condition);
    if (GroovyRuntimeUtil.isTruthy(result)) {
      if (feature.isParameterized()) {
        feature.addInterceptor(new PendingFeatureIterationInterceptor(annotation.exceptions(), annotation.reason(), PENDING_FEATURE_IF));
      } else {
        feature.getFeatureMethod().addInterceptor(
          new PendingFeatureInterceptor(annotation.exceptions(), annotation.reason(), PENDING_FEATURE_IF));
      }
    }
  }

  private Closure createCondition(Class<? extends Closure> clazz) {
    try {
      return clazz.getConstructor(Object.class, Object.class).newInstance(null, null);
    } catch (Exception e) {
      throw new ExtensionException("Failed to instantiate @PendingFeatureIf condition", e);
    }
  }

  private Object evaluateCondition(Closure condition) {
    condition.setDelegate(new PreconditionContext());
    condition.setResolveStrategy(Closure.DELEGATE_ONLY);

    try {
      return condition.call();
    } catch (Exception e) {
      throw new ExtensionException("Failed to evaluate @PendingFeatureIf condition", e);
    }
  }
}
