/*
 * Copyright 2017 the original author or authors.
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

package org.spockframework.runtime.extension.builtin;

import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;

import spock.lang.Retry;

/**
 * @author Leonard Brünings
 * @since 1.2
 */
public class RetryFeatureInterceptor extends RetryBaseInterceptor implements IMethodInterceptor {
  public RetryFeatureInterceptor(Retry retry) {
    super(retry);
  }

  @Override
  public void intercept(IMethodInvocation invocation) throws Throwable {
    handleInvocation(invocation);
  }
}
