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

package org.spockframework.mock;

import org.spockframework.util.Beta;
import org.spockframework.util.ThreadSafe;

import java.util.function.Supplier;

/**
 * A response strategy that delegates method calls to the real object underlying the mock (if any).
 */
@Beta
@ThreadSafe
public class CallRealMethodResponse implements IDefaultResponse {
  public static final CallRealMethodResponse INSTANCE = new CallRealMethodResponse();

  private CallRealMethodResponse() {}

  @Override
  public Supplier<Object> respond(IMockInvocation invocation) {
    return invocation::callRealMethod;
  }
}
