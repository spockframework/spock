/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spockframework.smoke.extension

import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.ParameterResolver
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.MethodInfo
import org.spockframework.runtime.model.ParameterInfo
import org.spockframework.runtime.model.SpecInfo

import java.lang.annotation.Target
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Target([ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER])
@Retention(RetentionPolicy.RUNTIME)
@ExtensionAnnotation(FastStubExtension)
@interface Fast {}

class FastStubExtension implements IAnnotationDrivenExtension<Fast> {
  @Override
  void visitParameterAnnotation(Fast annotation, ParameterInfo parameter) {
    parameter.parent?.addInterceptor(new ParameterResolver.Interceptor(parameter, { 42 }))
  }

  @Override
  void visitSpecAnnotations(List<Fast> annotations, SpecInfo spec) {
    // do nothing
  }

  @Override
  void visitFieldAnnotations(List<Fast> annotations, FieldInfo field) {
    // do nothing
  }

  @Override
  void visitFeatureAnnotations(List<Fast> annotations, FeatureInfo feature) {
    // do nothing
  }
}
