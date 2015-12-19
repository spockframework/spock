/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.AbstractRunListener
import org.spockframework.runtime.model.ErrorInfo

class VerifyExecutionExtension extends AbstractAnnotationDrivenExtension<VerifyExecution> {
  @Override
  void visitSpecAnnotation(VerifyExecution annotation, SpecInfo spec) {
    def executionLog = new ExecutionLog()
    def verifyMethod = spec.getFeatures().find { it.name == "verifyExecution" }
    if (!verifyMethod) {
      throw new ExtensionException("Spec is missing a 'verifyExecution' method.")
    }

    spec.addListener(new AbstractRunListener() {
      boolean hasErrors

      @Override
      void beforeFeature(FeatureInfo feature) {
        hasErrors = false
      }

      @Override
      void afterFeature(FeatureInfo feature) {
        if (hasErrors) {
          executionLog.failed << feature.name
        } else {
          executionLog.passed << feature.name
        }
      }

      @Override
      void error(ErrorInfo error) {
        hasErrors = true
      }

      @Override
      void featureSkipped(FeatureInfo feature) {
        executionLog.skipped << feature.name
      }
    })

    verifyMethod.addIterationInterceptor(new IMethodInterceptor() {
      @Override
      void intercept(IMethodInvocation invocation) throws Throwable {
        def variables = verifyMethod.getDataVariables()
        if (variables.size() == 1){
          invocation.getIteration().getDataValues().put(variables.get(0), executionLog);
        }
        invocation.proceed();
      }
    })
  }
}
