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

package org.spockframework.mock.runtime;

import org.spockframework.util.Beta;
import org.spockframework.util.Nullable;
import spock.config.ConfigurationObject;
import spock.mock.IMockMakerSettings;
import spock.mock.MockMakers;

/**
 * Configuration settings for Mock Makers.
 *
 * <p>Example:
 * <pre><code>
 * mockMaker {
 *   preferredMockMaker spock.mock.MockMakers.byteBuddy
 * }
 * </code>
 * </pre>
 *
 * @since 2.4
 */
@Beta
@ConfigurationObject("mockMaker")
public class MockMakerConfiguration {

  /**
   * The preferredMockMaker {@link IMockMaker} instance to use, when creating mock objects.
   * You can find the built-in mock makers in {@link MockMakers}.
   */
  @Nullable
  public IMockMakerSettings preferredMockMaker = null;
}
