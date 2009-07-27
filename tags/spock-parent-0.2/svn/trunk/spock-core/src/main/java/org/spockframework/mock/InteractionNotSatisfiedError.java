/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spockframework.mock;

import org.spockframework.runtime.SpeckAssertionError;

/**
 * Base class for exceptions indicating that one or more interactions
 * have not been satisfied (i.e. have seen to few or too many invocations).
 *
 * @author Peter Niederwieser
 */
abstract public class InteractionNotSatisfiedError extends SpeckAssertionError {}
