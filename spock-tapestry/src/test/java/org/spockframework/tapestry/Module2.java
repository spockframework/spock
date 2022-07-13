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

package org.spockframework.tapestry;

import org.apache.tapestry5.ioc.ServiceBinder;

// Tapestry module classes cannot currently be written in Groovy
// See https://issues.apache.org/jira/browse/TAPESTRY-2746
public class Module2 {
  public static void bind(ServiceBinder binder) {
    binder.bind(IService2.class, Service2.class);
  }
}
