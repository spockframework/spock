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

package org.spockframework.mock.runtime;

import org.spockframework.mock.IMockInteraction;
import org.spockframework.mock.IMockObject;
import org.spockframework.mock.ISpockMockObject;
import org.spockframework.util.Beta;
import org.spockframework.util.ThreadSafe;

/**
 * The {@link IMockInteractionValidator} interface allows {@link IMockMaker} implementations
 * to implement different kinds of validations on mock interactions.
 *
 * <p>The instance is registered and the {@link ISpockMockObject#$spock_mockInteractionValidator()} method. The {@code IMockMaker} shall return the validation instance to use.</p>
 *
 * @author Andreas Turban
 * @since 2.4
 */
@ThreadSafe
@Beta
public interface IMockInteractionValidator {
  /**
   * Is called by the {@link IMockInteraction} on creation to allow the underlying {@link IMockMaker} to validate the interaction.
   *
   * @param mockObject  the spock mock object
   * @param interaction the interaction to validate
   * @throws org.spockframework.runtime.InvalidSpecException if the interface is invalid
   * @since 2.4
   */
  @Beta
  void validateMockInteraction(IMockObject mockObject, IMockInteraction interaction);
}
