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

import org.spockframework.mock.CannotCreateMockException;
import org.spockframework.util.Nullable;
import org.spockframework.util.ReflectionUtil;
import org.spockframework.util.ThreadSafe;
import spock.mock.MockMakerId;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@ThreadSafe
public class ByteBuddyMockMaker implements IMockMaker {
  public static final MockMakerId ID = new MockMakerId("byte-buddy");
  public static final String IGNORE_PROP = "org.spockframework.mock.ignoreByteBuddy";
  private static final boolean ignoreByteBuddy = Boolean.getBoolean(IGNORE_PROP);
  private static final boolean byteBuddyAvailable = ReflectionUtil.isClassAvailable("net.bytebuddy.ByteBuddy");
  @Nullable
  private static final ByteBuddyMockFactory mockFactory = !ignoreByteBuddy && byteBuddyAvailable ? new ByteBuddyMockFactory() : null;
  private static final Set<MockMakerCapability> CAPABILITIES = Collections.unmodifiableSet(EnumSet.of(
    MockMakerCapability.INTERFACE,
    MockMakerCapability.CLASS,
    MockMakerCapability.ADDITIONAL_INTERFACES,
    MockMakerCapability.EXPLICIT_CONSTRUCTOR_ARGUMENTS
  ));

  @Override
  public MockMakerId getId() {
    return ID;
  }

  @Override
  public Set<MockMakerCapability> getCapabilities() {
    return CAPABILITIES;
  }

  @Override
  public int getPriority() {
    return 200;
  }

  @Override
  public Object makeMock(IMockCreationSettings settings) throws CannotCreateMockException {
    return requireNonNull(mockFactory).createMock(settings);
  }

  @Override
  public IMockabilityResult getMockability(IMockCreationSettings settings) {
    if (ignoreByteBuddy) {
      return () -> "Byte-buddy was ignored with " + IGNORE_PROP + ".";
    }
    if (!byteBuddyAvailable) {
      return () -> "The byte-buddy library is missing on the class path.";
    }
    return IMockabilityResult.MOCKABLE;
  }
}
