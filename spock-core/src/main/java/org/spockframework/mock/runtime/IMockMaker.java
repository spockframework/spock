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

import groovy.lang.Closure;
import org.spockframework.mock.CannotCreateMockException;
import org.spockframework.mock.IMockObject;
import org.spockframework.mock.ISpockMockObject;
import org.spockframework.mock.MockNature;
import org.spockframework.util.Beta;
import org.spockframework.util.Immutable;
import org.spockframework.util.Nullable;
import org.spockframework.util.ThreadSafe;
import spock.mock.MockMakers;

import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The {@code IMockMaker} provides a {@link ServiceLoader} extension point to allow the usage
 * of different implementations to create the underlying mock classes and mock instances.
 *
 * <p>The class {@link MockMakers} lists the well known {@code IMockMaker}s currently defined in Spock.
 *
 * <p>You can override the preferred {@code IMockMaker} with the {@link MockMakerConfiguration} in the Spock configuration file.
 *
 * <p>Note: The implementation must be thread-safe, because it can be called concurrently from different threads.
 *
 * @since 2.4
 */
@Beta
@ThreadSafe
public interface IMockMaker {

  /**
   * Returns the ID of this {@code IMockMaker}.
   * The ID must be unique.
   *
   * @return the ID
   */
  MockMakerId getId();

  /**
   * The {@link MockMakerCapability} set supported by this {@code IMockMaker}.
   *
   * <p>These capabilities are automatically checked and do not need to be checked in {@link #getMockability(IMockCreationSettings) getMockability}.
   *
   * @return the set of {@link MockMakerCapability}s which are supported by this {@code IMockMaker}.
   */
  Set<MockMakerCapability> getCapabilities();

  /**
   * The priority with which this {@code IMockMaker} is applied. A smaller value means higher priority.
   *
   * <p>The following priorities are reserved:
   * <ul>
   *   <li>{@code 0}: Reserved for the selected {@code IMockMaker} by {@link MockMakerConfiguration}.</li>
   * </ul>
   *
   * <p>Negative priorities are not allowed.</p>
   *
   * @return The priority with which this {@code IMockMaker} is applied.
   */
  int getPriority();

  /**
   * Creates a new mock object instance for the passed {@link IMockCreationSettings}.
   *
   * <p>Note: the call to this method is guarded by a call to {@link #getMockability(IMockCreationSettings) getMockability}, so you do not need to check it manually.</p>
   *
   * @param settings the settings
   * @return the new mock instance
   * @throws CannotCreateMockException if this {@code IMockMaker} can't create such a mock.
   */
  Object makeMock(IMockCreationSettings settings) throws CannotCreateMockException;

  /**
   * Creates a new static mock for the passed {@link IMockCreationSettings}.
   *
   * <p>Note: the call to this method is guarded by a call to {@link #getMockability(IMockCreationSettings) getMockability}, so you do not need to check it manually.</p>
   *
   * @param settings the settings
   * @return the new static mock
   * @throws CannotCreateMockException if this {@code IMockMaker} can't create such a mock.
   */
  default IStaticMock makeStaticMock(IMockCreationSettings settings) throws CannotCreateMockException {
    throw new UnsupportedOperationException("Operation not implemented by " + getId() + ", but claimed capability MockMakerCapability.STATIC_METHOD. Settings: " + settings);
  }

  /**
   * Checks whether {@link #makeMock(IMockCreationSettings) makeMock} would be able
   * to create a mock instance using the passed {@link IMockCreationSettings}.
   *
   * <p>Implementation Note: You do not need to check for the {@linkplain #getCapabilities() capabilities} this is done by the caller.
   *
   * @param settings the settings
   * @return whether a mock instance could be created or not with this {@code IMockMaker}
   */
  IMockabilityResult getMockability(IMockCreationSettings settings);

  /**
   * Returns information about a mock object, or {@code null} if the object is not a mock from this {@code IMockMaker}.
   *
   * <p>Implementation Note:
   * An {@code IMockMaker} must provide the means to identify if a certain object is a mock or not,
   * and needs to return an attached {@link IMockObject} from this method call.
   * If the mock instance implements {@link ISpockMockObject}, this method can simply return {@code null}.
   *
   * @param object a potential mock object
   * @return information about the mock object or {@code null}
   */
  default IMockObject asMockOrNull(Object object) {
    return null;
  }

  @Immutable
  @FunctionalInterface
  @Beta
  interface IMockabilityResult {
    IMockabilityResult MOCKABLE = new IMockabilityResult() {
      @Override
      public boolean isMockable() {
        return true;
      }

      @Override
      public String getNotMockableReason() {
        return "";
      }
    };

    /**
     * @return {@code true} if the {@link IMockCreationSettings} passed to one of the {@code makeMock} methods can be mocked.
     */
    default boolean isMockable() {
      return false;
    }

    /**
     * @return the reason why the {@link IMockCreationSettings} is not mockable, otherwise {@code null}.
     */
    @Nullable
    String getNotMockableReason();
  }

  /**
   * The {@code IMockCreationSettings} specify what the {@link IMockMaker} shall mock
   * in the methods {@link IMockMaker#makeMock(IMockCreationSettings) makeMock(...)}
   * or {@link IMockMaker#makeStaticMock(IMockCreationSettings) makeStaticMock(...)}.
   *
   * @since 2.4
   */
  @Beta
  interface IMockCreationSettings {

    Class<?> getMockType();

    MockNature getMockNature();

    List<Class<?>> getAdditionalInterface();

    @Nullable
    List<Object> getConstructorArgs();

    IProxyBasedMockInterceptor getMockInterceptor();

    /**
     * @return the classloader to loaded generated classes into. The {@link IMockMaker} can also choose another classloader, if necessary.
     */
    ClassLoader getClassLoader();

    /**
     * @return whether class-based mock objects should be instantiated with the Objenesis library (if available),
     * or by calling a real constructor, if the {@link IMockMaker} supports the Objenesis library.
     */
    boolean isUseObjenesis();

    /**
     * @return {@code true} if the mock created with these settings shall mock the static methods of the {@linkplain #getMockType() mock type}.
     */
    boolean isStaticMock();

    /**
     * @return the {@link IMockMakerSettings} passed from the user, or {@code null}.
     */
    @Nullable
    <T extends IMockMakerSettings> T getMockMakerSettings();
  }

  /**
   * Settings passed to the {@link IMockMaker} and created explicitly for the requested mock maker.
   * The user will create such an instance at the mocking site:
   *
   * <p>{@code Mock(mockMaker: yourMockSettings { yourProperty = true }, ClassToMock)}
   *
   * <p>The {@link IMockMaker} should define a static constant or method (see {@link MockMakers}),
   * which let the user construct a custom {@link IMockMakerSettings}.
   *
   * <p>If you provide a method, you can define your settings in a typesafe manner for the user,
   * e.g. with a {@link Closure} parameter to configure the mock.
   *
   * @since 2.4
   */
  @Immutable
  @Beta
  interface IMockMakerSettings {

    /**
     * Creates a simple {@code IMockMakerSettings} object with only an ID.
     *
     * @param id the mock maker ID.
     * @return the settings object
     */
    static IMockMakerSettings simple(MockMakerId id) {
      return new IMockMakerSettings() {
        @Override
        public MockMakerId getMockMakerId() {
          return id;
        }

        @Override
        public String toString() {
          return id.toString();
        }
      };
    }

    MockMakerId getMockMakerId();
  }

  /**
   * {@code MockMakerCapability} describes the possible features of an {@link IMockMaker}.
   *
   * <p>This provides the ability to extend the list of capabilities in the future without breaking existing external {@link IMockMaker}s.</p>
   *
   * @since 2.4
   */
  @Beta
  enum MockMakerCapability {
    /**
     * Is able to mock an interface type.
     */
    INTERFACE("Cannot mock interfaces."),
    /**
     * Is able to mock a class type.
     */
    CLASS("Cannot mock classes."),
    /**
     * Is able to mock a class/interface type with additional interfaces.
     */
    ADDITIONAL_INTERFACES("Cannot mock classes with additional interfaces."),
    /**
     * Is able to pass explicit constructor arguments to the class constructor.
     */
    EXPLICIT_CONSTRUCTOR_ARGUMENTS("Explicit constructor arguments are not supported."),
    /**
     * Is able to mock a final class type.
     */
    FINAL_CLASS("Cannot mock final classes."),
    /**
     * Is able to mock a final method.
     */
    FINAL_METHOD("Cannot mock final methods."),
    /**
     * Is able to mock a static method.
     *
     * <p>Note: This is currently not supported by spock-core, this is for future extension.</p>
     */
    STATIC_METHOD("Cannot mock static methods.");

    private final IMockabilityResult notMockable;

    MockMakerCapability(String errorMessage) {
      this.notMockable = () -> errorMessage;
    }

    /**
     * @return {@link IMockabilityResult} with the default error message for when this {@code MockMakerCapability} is not present.
     */
    IMockabilityResult notMockable() {
      return notMockable;
    }
  }

  /**
   * {@code IStaticMock} is returned by {@link IMockMaker#makeStaticMock(IMockCreationSettings) makeStaticMock(...)},
   * which allows {@link IMockMaker} to create static mocks, if supported.
   *
   * <p>The caller needs to enable the static mock with {@link #enable()}, before usage. And needs to {@link #disable()} after usage.
   *
   * <p>The implemented static mock requires to be implemented as thread-local behavior, so {@link #enable()} does only affect the current thread.
   *
   * @since 2.4
   */
  @Beta
  interface IStaticMock {

    /**
     * @return the static mocked type
     */
    Class<?> getType();

    /**
     * Enables the static mock.
     * Note: The default state on creation is disabled.
     */
    void enable();

    /**
     * Disables the static mock.
     * Note: The default state on creation is disabled.
     */
    void disable();
  }

  /**
   * Represents the ID of an {@link IMockMaker}.
   */
  @Immutable
  @Beta
  final class MockMakerId implements Comparable<MockMakerId> {
    private static final Pattern VALID_ID = Pattern.compile("^[a-z][a-z0-9-]*+(?<!-)$");
    private final String id;

    public MockMakerId(String id) {
      if (id == null) {
        throw new IllegalArgumentException("The ID is null.");
      }
      if (id.isEmpty()) {
        throw new IllegalArgumentException("The ID is empty.");
      }
      if (!VALID_ID.matcher(id).matches()) {
        throw new IllegalArgumentException("The ID '" + id + "' is invalid. Valid id must comply to the pattern: " + VALID_ID);
      }
      this.id = id;
    }

    @Override
    public int compareTo(MockMakerId o) {
      return this.id.compareTo(o.id);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      MockMakerId that = (MockMakerId) o;
      return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id);
    }

    @Override
    public String toString() {
      return id;
    }
  }
}
