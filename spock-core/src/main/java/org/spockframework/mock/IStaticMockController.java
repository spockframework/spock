/*
 * Copyright 2023 the original author or authors.
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

package org.spockframework.mock;

import org.spockframework.mock.runtime.IMockMaker;
import org.spockframework.util.Beta;

import java.util.concurrent.Callable;

/**
 * The {@code IStaticMockController} provides API to activate a static mocks on non-test {@link Thread Threads}.
 *
 * @since 2.4
 */
@Beta
public interface IStaticMockController {
  /**
   * Runs the code with the static mocks activated on the current {@link Thread}.
   *
   * <p>Note: You only need this if your current {@code Thread} is not the test thread.
   * On the test {@code Thread}, the static mocks is automatically activated.</p>
   *
   * @param code the code to execute
   */
  void runWithActiveStaticMocks(Runnable code);

  /**
   * Runs the code with the static mocks activated on the current {@link Thread}.
   *
   * <p>Note: You only need this if your current {@code Thread} is not the test thread.
   * On the test {@code Thread}, the static mocks is automatically activated.</p>
   *
   * @param <R>  the return type
   * @param code the code to execute
   * @return the return value of the executed code
   */
  <R> R withActiveStaticMocks(Callable<R> code);

  void registerStaticMock(IMockMaker.IStaticMock staticMock);
}
