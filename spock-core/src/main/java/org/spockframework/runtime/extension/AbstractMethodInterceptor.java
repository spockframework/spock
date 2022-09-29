/*
 * Copyright 2009 the original author or authors.
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
package org.spockframework.runtime.extension;

import org.spockframework.util.UnreachableCodeError;

@SuppressWarnings({"EmptyMethod", "RedundantThrows"})
public abstract class AbstractMethodInterceptor implements IMethodInterceptor {
  @Override
  public final void intercept(IMethodInvocation invocation) throws Throwable {
    switch(invocation.getMethod().getKind()) {
      case INITIALIZER:
        interceptInitializerMethod(invocation);
        break;
      case SHARED_INITIALIZER:
        interceptSharedInitializerMethod(invocation);
        break;
      case SETUP:
        interceptSetupMethod(invocation);
        break;
      case CLEANUP:
        interceptCleanupMethod(invocation);
        break;
      case SETUP_SPEC:
        interceptSetupSpecMethod(invocation);
        break;
      case CLEANUP_SPEC:
        interceptCleanupSpecMethod(invocation);
        break;
      case FEATURE:
        interceptFeatureMethod(invocation);
        break;
      case DATA_PROVIDER:
        interceptDataProviderMethod(invocation);
        break;
      case DATA_PROCESSOR:
        interceptDataProcessorMethod(invocation);
        break;
      case ITERATION_EXECUTION:
        interceptIterationExecution(invocation);
        break;
      case SPEC_EXECUTION:
        interceptSpecExecution(invocation);
        break;
      case FEATURE_EXECUTION:
        interceptFeatureExecution(invocation);
        break;
      default:
        throw new UnreachableCodeError();
    }
  }

  public void interceptInitializerMethod(IMethodInvocation invocation) throws Throwable {}
  public void interceptSharedInitializerMethod(IMethodInvocation invocation) throws Throwable {}
  public void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {}
  public void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {}
  public void interceptSetupSpecMethod(IMethodInvocation invocation) throws Throwable {}
  public void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {}
  public void interceptFeatureMethod(IMethodInvocation invocation) throws Throwable {}
  public void interceptDataProviderMethod(IMethodInvocation invocation) throws Throwable {}
  public void interceptDataProcessorMethod(IMethodInvocation invocation) throws Throwable {}
  public void interceptIterationExecution(IMethodInvocation invocation) throws Throwable {}
  public void interceptSpecExecution(IMethodInvocation invocation) throws Throwable {}
  public void interceptFeatureExecution(IMethodInvocation invocation) throws Throwable {}
}
