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

package org.spockframework.runtime.extension;

import org.spockframework.runtime.model.SpecInfo;

public interface IGlobalExtension {
  /**
   * Is called when Spock starts it services, before tests are discovered.
   */
  default void start() {}

  /**
   * This is called for each {@link spock.lang.Specification} discovered.
   * <p>
   * Extensions can perform their actions to register {@link IMethodInterceptor} or other things.
   * @param spec the {@link SpecInfo} for the discovered {@link spock.lang.Specification}.
   *             Note that this is always the bottomSpec, it is not called for super specs.
   */
  default void visitSpec(SpecInfo spec) {}

  /**
   * Is called right before Spock starts executing tests.
   * <p>
   * Extensions get access to the {@link IStore} via the {@link ISpockExecution}
   * and can initialize things that are needed during execution.
   * @since 2.4
   */
  default void executionStart(ISpockExecution spockExecution) {}
  /**
   * Is called after Spock finished executing tests.
   * <p>
   * Extensions get access to the {@link IStore} via the {@link ISpockExecution}
   * and can do some finishing action.
   * <p>
   * Note that values implementing {@link AutoCloseable} will be automatically closed
   * by the {@link IStore}, so there is no need to do this here.
   * @since 2.4
   */
  default void executionStop(ISpockExecution spockExecution) {}

  /**
   * Is called when the execution stops, or when the JVM exits at the latest.
   * <p>
   * It can be called more than once.
   */
  default void stop() {}
}
