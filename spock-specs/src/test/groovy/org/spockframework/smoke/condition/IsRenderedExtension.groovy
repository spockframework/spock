/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.smoke.condition

import org.spockframework.runtime.extension.IAnnotationDrivenExtension

import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Retention

import org.opentest4j.AssertionFailedError
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.ConditionNotSatisfiedError
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.extension.ExtensionAnnotation

@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(IsRenderedExtension)
@interface IsRendered {
  String value()
}

class IsRenderedExtension implements IAnnotationDrivenExtension<IsRendered> {
  @Override
  void visitFeatureAnnotation(IsRendered annotation, FeatureInfo feature) {
    feature.featureMethod.interceptors.add({ IMethodInvocation invocation ->
      try {
        invocation.proceed()
        assert "@IsRendered only works for failing conditions (but no condition failed)"
      } catch (ConditionNotSatisfiedError e) {
        def expected = annotation.value().trim()
        def actual = e.condition.rendering.trim()
        if (expected != actual) throw new AssertionFailedError("Condition rendered incorrectly", expected, actual)
      }
    } as IMethodInterceptor)
  }
}
