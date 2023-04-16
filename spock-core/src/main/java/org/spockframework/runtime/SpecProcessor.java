/*
 * Copyright 2023 the original author or authors.
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

package org.spockframework.runtime;

import org.spockframework.runtime.extension.builtin.orderer.SpecOrderer;
import org.spockframework.runtime.model.SpecInfo;

import java.util.Collection;

/**
 * Generic bulk processor for a collection of {@link SpecInfo} elements
 *
 * @see SpecOrderer
 */
public interface SpecProcessor {
  /**
   * Bulk-process a collection of {@link SpecInfo} elements in-place, i.e. do not return anything but operate on the
   * elements given, changing their state if necessary.
   *
   * @param specs spec-info instances to be processed
   */
  void process(Collection<SpecInfo> specs);
}
