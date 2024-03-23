/*
 * Copyright 2010 the original author or authors.
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

package spock.config;

/**
 * Configuration settings for the spec runner.
 *
 * <p>Example:
 * <pre>
 * import some.pkg.Fast
 * import some.pkg.IntegrationSpec
 *
 * runner {
 *   include Fast // could be either an annotation or a (base) class
 *   exclude {
 *     annotation some.pkg.Slow
 *     baseClass IntegrationSpec
 *   }
 *   filterStackTrace true // this is the default
 *   matchFirstInteraction true // this is the default
 * }
 * </pre>
 */
@ConfigurationObject("runner")
public class RunnerConfiguration {
  public IncludeExcludeCriteria include = new IncludeExcludeCriteria();
  public IncludeExcludeCriteria exclude = new IncludeExcludeCriteria();
  public ParallelConfiguration parallel = new ParallelConfiguration();
  public boolean filterStackTrace = true;
  public boolean optimizeRunOrder = false;
  public boolean matchFirstInteraction = true;
}
