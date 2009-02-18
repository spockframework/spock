/*
 * Copyright 2009 the original author or authors.
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

package org.spockframework.runtime.intercept;

/**
 * A ...
 *
 * @author Peter Niederwieser
 */
// IDEA: could separate exceptions thrown by Speck code from exceptions thrown
// by an interceptor (although an interceptor might again call into Speck code,
// or at least act on behalf of the Speck code, e.g. by connecting to a database)
// possibilities for separation: 1. wrap Speck exceptions 2. pass back Speck
// exceptions as return value of invoke()
public interface IMethodInterceptor {
  void invoke(MethodInvocation invocation) throws Throwable;
}
