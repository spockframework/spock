/*
 * Copyright 2026 the original author or authors.
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

package spock.mock;

import org.spockframework.util.Beta;
import spock.lang.Specification;

import java.util.concurrent.Callable;

/**
 * Implement to encapsulate mock creation and mock interactions in a reusable
 * fixture object that lives outside a {@code Specification}. The implementor
 * supplies the owning spec via {@link #getSpecification()} (for example from a
 * constructor field); all created mocks and declared interactions are routed
 * through that spec's mock controller.
 *
 * <p>Both creation and interactions work: {@code Mock(Bar)} is a real,
 * resolvable method inherited from {@link MockingApi}, so it compiles,
 * autocompletes, and survives {@code @CompileStatic}. The Spock compiler
 * replaces the call with one that creates and auto-attaches the mock to the
 * located spec.
 *
 * <p>A class that is both a {@code Specification} and implements this interface
 * is a compile error: the spec already has the full capability.
 *
 * @since 2.5
 */
@Beta
public interface MockInteractionSupport extends MockingApi {
  /**
   * The owning specification used to create mocks and register interactions.
   *
   * @return the owning specification
   */
  Specification getSpecification();

  @Override
  default void runWithThreadAwareMocks(Runnable code) {
    getSpecification().getSpecificationContext().getThreadAwareMockController().runWithThreadAwareMocks(code);
  }

  @Override
  default <R> R withActiveThreadAwareMocks(Callable<R> code) {
    return getSpecification().getSpecificationContext().getThreadAwareMockController().withActiveThreadAwareMocks(code);
  }
}
