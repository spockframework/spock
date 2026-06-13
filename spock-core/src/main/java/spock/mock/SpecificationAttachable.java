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

/**
 * An object that can be attached to and detached from a {@link Specification}.
 *
 * <p>Spock's mock objects implement this interface internally. A user class
 * (for example a {@link MockInteractionSupport} fixture) may implement it to
 * receive the running spec from the {@link AutoAttach} extension, which calls
 * {@link #attach} during {@code setup} and {@link #detach} during
 * {@code cleanup}.
 *
 * @since 2.5
 */
@Beta
public interface SpecificationAttachable {

  /**
   * Attaches this object to a specification.
   *
   * @param specification the specification this object should be attached to
   */
  void attach(Specification specification);

  /**
   * Detaches this object from its current specification.
   */
  void detach();
}
