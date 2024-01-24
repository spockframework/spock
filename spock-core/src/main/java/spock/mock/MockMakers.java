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

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.SimpleType;
import org.mockito.MockSettings;
import org.spockframework.mock.runtime.ByteBuddyMockMaker;
import org.spockframework.mock.runtime.CglibMockMaker;
import org.spockframework.mock.runtime.IMockMaker;
import org.spockframework.mock.runtime.JavaProxyMockMaker;
import org.spockframework.mock.runtime.mockito.MockitoMockMaker;
import org.spockframework.mock.runtime.mockito.MockitoMockMakerSettings;

import java.lang.reflect.Proxy;

import static spock.mock.IMockMakerSettings.settingsFor;

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
  public static final IMockMakerSettings byteBuddy = settingsFor(ByteBuddyMockMaker.ID);

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
  public static final IMockMakerSettings cglib = settingsFor(CglibMockMaker.ID);

  /**
   * Uses the Java {@link Proxy} API to create mocks of interfaces.
   *
   * <p>The supported mocking features are:
   * <ul>
   * <li>{@code INTERFACE}</li>
   * <li>{@code ADDITIONAL_INTERFACES}</li>
   * </ul>
   */
  public static final IMockMakerSettings javaProxy = settingsFor(JavaProxyMockMaker.ID);

  /**
   * Uses <a href="https://site.mockito.org/">Mockito</a> to create mocks,
   * which also supports final classes, enums and final methods.
   *
   * <p>The supported mocking features are:
   * <ul>
   * <li>{@code INTERFACE}</li>
   * <li>{@code CLASS}</li>
   * <li>{@code ADDITIONAL_INTERFACES}</li>
   * <li>{@code EXPLICIT_CONSTRUCTOR_ARGUMENTS}</li>
   * <li>{@code FINAL_CLASS}</li>
   * <li>{@code FINAL_METHOD}</li>
   *
   * <p>It uses {@link org.mockito.MockMakers#INLINE} under the hood,
   * please see the Mockito manual for all pros and cons, when using {@code MockMakers.INLINE}.
   */
  public static final IMockMakerSettings mockito = settingsFor(MockitoMockMaker.ID);

  /**
   * Uses <a href="https://site.mockito.org/">Mockito</a> to create mocks,
   * which also supports final classes, enums and final methods.
   *
   * <p>The supported mocking features are:
   * <ul>
   * <li>{@code INTERFACE}</li>
   * <li>{@code CLASS}</li>
   * <li>{@code ADDITIONAL_INTERFACES}</li>
   * <li>{@code EXPLICIT_CONSTRUCTOR_ARGUMENTS}</li>
   * <li>{@code FINAL_CLASS}</li>
   * <li>{@code FINAL_METHOD}</li>
   *
   * <p>It uses {@link org.mockito.MockMakers#INLINE} under the hood,
   * please see the Mockito manual for all pros and cons, when using {@code MockMakers.INLINE}.
   *
   * @param settingsCode the code to execute to configure {@link MockSettings} for further configuration of the mock to create
   */
  public static IMockMakerSettings mockito(@DelegatesTo(type = "org.mockito.MockSettings", strategy = Closure.DELEGATE_FIRST)
                                           @ClosureParams(value = SimpleType.class, options = "org.mockito.MockSettings")
                                           Closure<?> settingsCode) {
    return MockitoMockMakerSettings.createSettings(settingsCode);
  }
}
