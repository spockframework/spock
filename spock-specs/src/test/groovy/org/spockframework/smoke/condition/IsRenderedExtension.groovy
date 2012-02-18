package org.spockframework.smoke.condition

import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Retention

import org.junit.ComparisonFailure
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.ExtensionAnnotation

@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(IsRenderedExtension)
@interface IsRendered {
  String value()
}

class IsRenderedExtension extends AbstractAnnotationDrivenExtension<IsRendered> {
  @Override
  void visitFeatureAnnotation(IsRendered annotation, FeatureInfo feature) {
    feature.featureMethod.interceptors.add({ IMethodInvocation invocation ->
      try {
        invocation.proceed()
        assert "@IsRendered only works for failing conditions (but no condition failed)"
      } catch (ConditionNotSatisfiedError e) {
        def expected = annotation.value().trim()
        def actual = e.condition.rendering.trim()
        if (expected != actual) throw new ComparisonFailure("Condition rendered incorrectly", expected, actual)
      }
    } as IMethodInterceptor)
  }
}