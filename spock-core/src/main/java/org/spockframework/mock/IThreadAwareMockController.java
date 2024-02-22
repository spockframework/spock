/*
 * Copyright 2024 the original author or authors.
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

import org.spockframework.mock.runtime.IMockMaker.IStaticMock;
import org.spockframework.util.Beta;

import java.util.concurrent.Callable;

/**
 * The {@code IThreadAwareMockController} provides API to activate a thread-aware mocks on non-test {@link Thread Threads}.
 *
 * <p>Thread-aware mocks are:
 * <ul>
 * <li>Static mocks</li>
 * </ul>
 *
 * @since 2.4
 */
@Beta
public interface IThreadAwareMockController {
  /**
   * Runs the code with all thread-aware mocks activated on the current {@link Thread}.
   *
   * <p>Note: You only need this if your current {@code Thread} is not the test thread.
   * On the test {@code Thread}, the thread-aware mocks are automatically activated.</p>
   *
   * @param code the code to execute
   */
  void runWithThreadAwareMocks(Runnable code);

  /**
   * Runs the code with all thread-aware mocks activated on the current {@link Thread}.
   *
   * <p>Note: You only need this if your current {@code Thread} is not the test thread.
   * On the test {@code Thread}, the thread-aware mocks are automatically activated.</p>
   *
   * @param <R>  the return type
   * @param code the code to execute
   * @return the return value of the executed code
   */
  <R> R withActiveThreadAwareMocks(Callable<R> code);

  /**
   * Registers the passed {@link IStaticMock} as a thread-aware mock to {@code this} instance.
   *
   * @param staticMock the mock to register
   */
  void registerStaticMock(IStaticMock staticMock);
}
