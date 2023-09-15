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

package spock.mock;

import org.spockframework.mock.runtime.ByteBuddyMockMaker;
import org.spockframework.mock.runtime.CglibMockMaker;
import org.spockframework.mock.runtime.IMockMaker;
import org.spockframework.mock.runtime.JavaProxyMockMaker;

import java.lang.reflect.Proxy;

import static org.spockframework.mock.runtime.IMockMaker.IMockMakerSettings.simple;

/**
 * Provides constants and factory methods for known built-in {@link IMockMaker} implementations.
 *
 * <p>You can select the mock maker during mock creation:
 * {@code YourClass yourMock = Mock(mockMaker: MockMakers.byteBuddy)}
 *
 * @since 2.4
 */
public final class MockMakers {
  private MockMakers() {
  }

  /**
   * Uses <a href="https://bytebuddy.net/">Byte Buddy</a> to create mocks.
   *
   * <p>The supported mocking features are:
   * <ul>
   * <li>{@code INTERFACE}</li>
   * <li>{@code CLASS}</li>
   * <li>{@code ADDITIONAL_INTERFACES}</li>
   * <li>{@code EXPLICIT_CONSTRUCTOR_ARGUMENTS}</li>
   * </ul>
   */
  public static final IMockMaker.IMockMakerSettings byteBuddy = simple(ByteBuddyMockMaker.ID);

  /**
   * Uses <a href="https://github.com/cglib/cglib">CGLIB</a> to create mocks.
   *
   * <p>The supported mocking features are:
   * <ul>
   * <li>{@code INTERFACE}</li>
   * <li>{@code CLASS}</li>
   * <li>{@code ADDITIONAL_INTERFACES}</li>
   * <li>{@code EXPLICIT_CONSTRUCTOR_ARGUMENTS}</li>
   * </ul>
   */
  public static final IMockMaker.IMockMakerSettings cglib = simple(CglibMockMaker.ID);

  /**
   * Uses the Java {@link Proxy} API to create mocks of interfaces.
   *
   * <p>The supported mocking features are:
   * <ul>
   * <li>{@code INTERFACE}</li>
   * <li>{@code ADDITIONAL_INTERFACES}</li>
   * </ul>
   */
  public static final IMockMaker.IMockMakerSettings javaProxy = simple(JavaProxyMockMaker.ID);
}
