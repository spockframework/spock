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

package org.spockframework.runtime.extension.builtin;

import org.spockframework.util.Beta;
import spock.config.ConfigurationObject;
import spock.config.IncludeExcludeCriteria;

/**
 * Configuration settings for the unroll extension.
 *
 * <p>Example:
 * <pre>
 * unroll {
 *   enabledByDefault false // default true
 *   defaultPattern '#featureName[#iterationIndex]' // default null
 *   expressionsAsserted false // default true
 * }
 * </pre>
 *
 * @since 2.0
 */
@Beta
@ConfigurationObject("unroll")
public class UnrollConfiguration {
  public boolean enabledByDefault = true;
  public String defaultPattern = null;
  public boolean expressionsAsserted = true;
}
