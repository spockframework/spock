/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.builder;

import java.util.List;

// IDEA: provide getID() for generic error reporting; could be the path to the target
public interface IConfigurationTarget {
  Object getSubject();
  IConfigurationTarget readSlot(String name);
  void writeSlot(String name, Object value);
  // TODO: support return value (e.g. to save off created object in a variable)?
  // TODO: should be List<?>, otherwise caller get problems
  void configureSlot(String name, List<Object> values, IConfigurationSource source);
}
