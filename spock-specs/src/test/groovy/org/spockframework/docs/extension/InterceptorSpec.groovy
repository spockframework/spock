/*
 * Copyright 2024 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.spockframework.docs.extension

import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.ExtensionAnnotation
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Specification

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@InterceptorDemo
class InterceptorSpec extends Specification {
  @InterceptorDemo
  def "a method"() {
    expect: true
  }
}

// tag::interceptor-class[]
class I extends AbstractMethodInterceptor {
  I(def s) {}
}
// end::interceptor-class[]

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE, ElementType.METHOD])
@ExtensionAnnotation(InterceptorDemoExtension)
@interface InterceptorDemo {}

class InterceptorDemoExtension implements IAnnotationDrivenExtension<InterceptorDemo> {
  @Override
  void visitSpecAnnotation(InterceptorDemo annotation, SpecInfo specInfo) {
// tag::interceptor-register-spec[]
    // DISCLAIMER: The following shows all possible injection points that you could use
    //             depending on need and situation. You should normally not need to
    //             register a listener to all these places.
    //
    //             Also, when building an annotation driven local extension, you should
    //             consider where you want the effects to be present, for example only
    //             for the features in the same class (specInfo.features), or for features
    //             in the same and superclasses (specInfo.allFeatures), or also for
    //             features in subclasses (specInfo.bottomSpec.allFeatures), and so on.

    // on SpecInfo
    specInfo.specsBottomToTop*.addSharedInitializerInterceptor new I('shared initializer')
    specInfo.allSharedInitializerMethods*.addInterceptor new I('shared initializer method')
    specInfo.addInterceptor new I('specification')
    specInfo.specsBottomToTop*.addSetupSpecInterceptor new I('setup spec')
    specInfo.allSetupSpecMethods*.addInterceptor new I('setup spec method')
    specInfo.allFeatures*.addInterceptor new I('feature')
    specInfo.specsBottomToTop*.addInitializerInterceptor new I('initializer')
    specInfo.allInitializerMethods*.addInterceptor new I('initializer method')
    specInfo.allFeatures*.addIterationInterceptor new I('iteration')
    specInfo.specsBottomToTop*.addSetupInterceptor new I('setup')
    specInfo.allSetupMethods*.addInterceptor new I('setup method')
    specInfo.allFeatures*.featureMethod*.addInterceptor new I('feature method')
    specInfo.specsBottomToTop*.addCleanupInterceptor new I('cleanup')
    specInfo.allCleanupMethods*.addInterceptor new I('cleanup method')
    specInfo.specsBottomToTop*.addCleanupSpecInterceptor new I('cleanup spec')
    specInfo.allCleanupSpecMethods*.addInterceptor new I('cleanup spec method')
    specInfo.allFixtureMethods*.addInterceptor new I('fixture method')
// end::interceptor-register-spec[]
  }

  @Override
  void visitFeatureAnnotation(InterceptorDemo annotation, FeatureInfo featureInfo) {
// tag::interceptor-register-feature[]
    // on FeatureInfo (already included above, handling all features)
    featureInfo.addInterceptor new I('feature')
    featureInfo.addIterationInterceptor new I('iteration')
    featureInfo.featureMethod.addInterceptor new I('feature method')

    // since Spock 2.4 there are also feature-scoped interceptors that only apply for a single feature
    // they will execute before the spec interceptors
    featureInfo.addInitializerInterceptor new I('feature scoped initializer')
    featureInfo.addSetupInterceptor new I('feature scoped setup')
    featureInfo.addCleanupInterceptor new I('feature scoped cleanup')
    // you can also perform a feature-scoped interception of spec methods
    featureInfo.parent.allInitializerMethods.each { method ->
      featureInfo.addScopedMethodInterceptor(method, new I('feature scoped initializer method'))
    }
    featureInfo.parent.allSetupMethods.each { method ->
      featureInfo.addScopedMethodInterceptor(method, new I('feature scoped setup method'))
    }
    featureInfo.parent.allCleanupMethods.each { method ->
      featureInfo.addScopedMethodInterceptor(method, new I('feature scoped cleanup method'))
    }

// end::interceptor-register-feature[]
  }
}
